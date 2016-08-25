/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * ProjectToTable.groovy
 *
 * Export multiple sessions to a single table for export to CSV.
 *
 * @date 1 May 2015
 * @author Greg Hedlund &lt;ghedlund@mun.ca&gt;
 */
import ca.phon.session.*;
import ca.phon.app.session.*;
import ca.phon.ipa.*;

import javax.swing.*;

def project = window.project;
if(project == null) return;

// Display session selector and obtain a list of selected session locations.
def sessionSelector = new SessionSelector(project);
def scroller = new JScrollPane(sessionSelector);
JOptionPane.showMessageDialog(window, scroller);

def selectedSessions = sessionSelector.selectedSessions;
if(selectedSessions.size() == 0) return;

// collect tiers
def tiers = ["Orthography", "IPA Target", "IPA Actual", "Segment", "Notes"];
selectedSessions.each { sessionLoc ->
	def session = project.openSession(sessionLoc.corpus, sessionLoc.session);
	session.userTiers.each { userTier ->
		if(!tiers.contains(userTier.name)) tiers.add(userTier.name);
	}
}

// print column header
print "\"Session\",\"Record #\",\"Speaker\"";
tiers.each { tierName -> print ",\"$tierName\""; }
print "\n"

// print data
selectedSessions.each { sessionLoc ->
	def session = project.openSession(sessionLoc.corpus, sessionLoc.session);
	session.records.each { record ->
		print "\"$sessionLoc\",\"" + 
			(session.getRecordPosition(record) + 1) + "\"";
		print ",\"$record.speaker\"";
		
		tiers.each { tierName ->
			tier = record.getTier(tierName, String.class);
			tierData = (tier != null ? tier.toString() : "");
			print ",\"$tierData\"";
		}
		print "\n"
	}
}


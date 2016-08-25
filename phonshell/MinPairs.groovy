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
 * Find IPA Target/Actual min-pairs for selected sessions.
 *
 * @date 4 April 2015
 * @author Greg Hedlund &lt;ghedlund@mun.ca&gt;
 */
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import ca.phon.ipa.*;
import ca.phon.ipa.features.CompoundFeatureComparator;
import ca.phon.ipa.features.FeatureComparator;
import ca.phon.ipa.tree.*;
import ca.phon.app.session.*;

def project = window.project;
if(project == null) return;

// Display session selector and obtain a list of selected session locations.
def sessionSelector = new SessionSelector(project);
def scroller = new JScrollPane(sessionSelector);
JOptionPane.showMessageDialog(window, scroller);

def selectedSessions = sessionSelector.selectedSessions;
if(selectedSessions.size() == 0) return;

// IpaTernaryTree is an implementation of a ternary tree which
// accepts IPATranscript objects.  It is fast, however
// the algorithm in this script is slow - O(n^2)
minPairs = new IpaTernaryTree();

selectedSessions.each { sessionLoc ->
	def session = project.openSession(sessionLoc.corpus, sessionLoc.session);
	session.records.each{ record ->
		record.IPAActual.each { group ->
			group.words().findAll{it.contains("\\w")}.each {
				minPairs.put(it, new HashSet<IPATranscript>());
			}
		}
	}	
}

def keySet = minPairs.keySet();

// find min-pairs
keySet.each{ key ->
	def pairs = minPairs.get(key);
	
	keySet.each{ key2 ->
		if(key == key2) return;
		
		if(key.getExtension(LevenshteinDistance).distance(key2) == 1) {
			pairs.add(key2);
		}
	}
}

// print report
keySet.each { key ->
	def pairs = minPairs.get(key);
	
	if(pairs.size() > 0) {
		println "$key:$pairs"
	}
}

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
 * Create a new single-record session for each record in
 * the selected session.
 *
 * @date 23 Oct 2015
 * @author Greg Hedlund &lt;ghedlund@mun.ca&gt;
 */
import ca.phon.ipa.*;
import ca.phon.session.*;
import ca.phon.extensions.*;
import ca.phon.app.session.*;
import ca.phon.ui.toast.*;
import ca.phon.project.*;

import javax.swing.*;

def project = window.project;
if(project == null) ToastFactory.makeToast("No project").start();

def sessionSelector = new SessionSelector(project);
JOptionPane.showMessageDialog(null, new JScrollPane(sessionSelector));

def sessionFactory = SessionFactory.newFactory();

def selectedSessions = sessionSelector.getSelectedSessions();
selectedSessions.each { sessionLoc ->
	def corpusName = sessionLoc.corpus + "_" + sessionLoc.session;
	def session = project.openSession(sessionLoc.corpus, sessionLoc.session);

	if(!project.getCorpora().contains(corpusName)) {
		project.addCorpus(corpusName, "Exploded session data for " + sessionLoc);
	}

	session.records.each { record ->
		def rIdx = session.getRecordPosition(record)+1;

		def newSession = sessionFactory.createSession();

		sessionFactory.copySessionInformation(session, newSession);
		sessionFactory.copySessionMetadata(session, newSession);
		sessionFactory.copySessionTierInformation(session, newSession);

		session.participants.each { part ->
			def clonedPart = sessionFactory.cloneParticipant(part);
			newSession.addParticipant(clonedPart);
		}
		
		newSession.setCorpus(corpusName);
		newSession.setName(String.format("%04d", rIdx));

		newSession.addRecord(record);

		try {
			def writeLock = project.getSessionWriteLock(newSession);
			project.saveSession(newSession, writeLock);
			project.releaseSessionWriteLock(newSession, writeLock);
		} catch (e) {
			println "\"$sessionLoc\",\"0\",\"0\",\"$e.message\"";	
		}
	}
}



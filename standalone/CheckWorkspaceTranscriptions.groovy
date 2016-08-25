#!/usr/bin/groovy
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
 * CheckWorkspaceTranscriptions.groovy
 * 
 * Check IPA Transcriptions for all workspace projects.
 
 * @author Greg Hedlund &lt;ghedlund@mun.ca&gt;
 * @date 10 Oct 2014 
 */

/*
 * These lines grab the necessary phon depenencies.
 * The first time the script executes, there may be a delay
 * while modules are downloading (requires Internet access.)
 */
@GrabResolver(name='phon', root='https://phon.ca/artifacts/')
/*
 * Use module 'phon-app' to grab all of phon
 */
@Grab(group='ca.phon', module='phon-workspace', version='2.1.8')

import ca.phon.workspace.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.extensions.*;

void checkIPA(project, session, record, ipaTier) {
	ipaTier.each{ group ->
		if(group != null && group.getExtension(UnvalidatedValue.class) != null) {
			def uv = group.getExtension(UnvalidatedValue.class);
			println "\"$project.name\",\"$session.corpus/$session.name\",\"" + session.getRecordPosition(record) + "\",\"$ipaTier.name\",\"$uv.value\",\"$uv.parseError.message\"";
		}
	}
}

void checkOrthography(project, session, record, orthoTier) {
	// check orthography for matching braces
	int cnt = 0;
	orthoTier.each { group ->
		group.toString().toCharArray().each{ c -> 
			if(c == '{') ++cnt;
			if(c == '}') --cnt;
		}
	}
	if(cnt != 0) {
		// print warning
		println "\"$project.name\",\"$session.corpus/$session.name\",\"" + session.getRecordPosition(record) + "\",\"$orthoTier.name\",\"$orthoTier\",\"Inner-group braces un-balanced\"";	
	}
}

println "\"Project\",\"Session\",\"Tier\",\"Value\",\"Error\"";
def workspace = Workspace.userWorkspace();
workspace.projects.each{ project ->
	project.corpora.each{ corpus ->
		project.getCorpusSessions(corpus).each{ sessionName ->
			try {
				def session = project.openSession(corpus, sessionName);
				session.records.each{ record -> 
					checkOrthography(project, session, record, record.orthography);
					checkIPA(project, session, record, record.IPATarget);
					checkIPA(project, session, record, record.IPAActual);
				}
			} catch (e) {
				println("Unable to open $corpus/$sessionName:" + e.getMessage());
			}
		}
	}
} 


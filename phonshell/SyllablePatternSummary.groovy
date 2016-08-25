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
 * SyllablePatternSummary.groovy
 * 
 * @author Greg Hedlund &lt;ghedlund@mun.ca&gt;
 * @date 19 Aug 2015
 */
import ca.phon.ipa.*
import ca.phon.ui.toast.*
import ca.phon.session.*
import ca.phon.app.session.*
import ca.phon.util.*
import javax.swing.*

	// running inside the application
	project = window.project
	if(project == null) ToastFactory.makeToast("No project").start()
	def sessionSelector = new SessionSelector(project);
	JOptionPane.showMessageDialog(null, new JScrollPane(sessionSelector));


/**
 * Return a String representation of syllable pattern
 * in XYZ format.
 */
def syllablePattern(IPATranscript ipa) {
	def syllList = []

	currentChar = '@';
	StringBuilder sylls = new StringBuilder();
	StringBuilder sb = new StringBuilder();
	ipa.syllables().each{ syll ->
		if(sylls.size() > 0) sylls.append(".");		
		sylls.append(syll.removePunctuation().toString());

		strippedSyll = syll.stripDiacritics().removePunctuation();
		if(!syllList.contains(strippedSyll.toString())) {
			currentChar++;
			syllList.add(strippedSyll.toString());
		}
		sb.append( (char) (65 + syllList.indexOf(strippedSyll.toString())) );
	}
	return [ sylls.toString(), sb.toString() ];
}

def patternSet = new java.util.TreeSet<String>();
def searchData = new java.util.TreeMap<String, java.util.Map>();

def selectedSessions = sessionSelector.selectedSessions;
selectedSessions.each{ sessionLoc ->
	def session = project.openSession(sessionLoc.corpus, sessionLoc.session);

	// map of <SyllPattern> -> [<NumCorrect>,<Total>]
	def sessionMap = new java.util.TreeMap<String, int[]>();
	searchData.put(sessionLoc.toString(), sessionMap);

	session.records.each{ record ->
		for(grpIdx = 0; grpIdx < record.numberOfGroups(); grpIdx++) {
			def group = record.getGroup(grpIdx);
			def includeGroup = true;
			
			ipaT = group.IPATarget;
			ipaT.syllables().each { includeGroup &= it.contains("\\v") }
			if(ipaT.toString().contains("C") || ipaT.toString().contains("V")
				|| ipaT.toString().contains("cv")) includeGroup = false;
			(ipaT, ipaTPattern) = syllablePattern(ipaT);
			
			ipaA = group.IPAActual;
			ipaA.syllables().each { includeGroup &= it.contains("\\v") }
			if(ipaA.toString().contains("C") || ipaA.toString().contains("V") 
				|| ipaA.toString().contains("cv")) includeGroup = false;
			(ipaA, ipaAPattern) = syllablePattern(ipaA);
			
			rIdx = session.getRecordPosition(record)+1;
			gIdx = grpIdx + 1;
			matches = (ipaTPattern == ipaAPattern ? "Y" : "N");
			if(includeGroup) {
				patternSet.add(ipaTPattern);
				int[] counts = sessionMap.get(ipaTPattern);
				if(counts == null) {
					counts = new int[3];
					sessionMap.put(ipaTPattern, counts);
				}
				// increment target count
				counts[0]++;
				if(matches) counts[2]++;
				
				patternSet.add(ipaAPattern);
				counts = sessionMap.get(ipaAPattern);
				if(counts == null) {
					counts = new int[3];
					sessionMap.put(ipaAPattern, counts);
				}
				// increment actual count
				counts[1]++;
				
			}
		}
	}
}


// print table header
print "\"Session Name\""
patternSet.each{ pattern ->
	print ",\"$pattern (Target)\",\"$pattern (Actual)\",\"$pattern (Matches)\""
}
println()

// print data
searchData.keySet().each { sessionName ->
	print "\"$sessionName\""

	sessionMap = searchData.get(sessionName);
	patternSet.each{ pattern ->
		counts = sessionMap.get(pattern);
		if(counts == null) {
			counts = [ 0, 0, 0];
		}
		tcnt = counts[0];
		acnt = counts[1];
		mcnt = counts[2];
		print ",\"$tcnt\",\"$acnt\",\"$mcnt\""
	}
	println()
}


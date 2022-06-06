/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.ext.textprocessing.data.Tag;

public enum SpacyNerTag {
	LOC("placeName", "GPE_LOC"), //
	ORG("orgName", "ORGANIZATION", "GPE_ORG"), //
	EVENT("EVT"), //
	GPE("geogName"), //
	PERSON("PER", "persName"), //
	PRODUCT("PROD"), //
	CARDINAL, DATE, FAC, LANGUAGE, LAW, MONEY, NORP, ORDINAL, PERCENT, QUANTITY, TIME, WORK_OF_ART, MOVEMENT, PET_NAME,
	PHONE, TITLE_AFFIX, DATETIME, FACILITY, NAT_REL_POL, NUMERIC_VALUE, PERIOD, DRV, MISC, UNKNOWN;

	public static final String TAG_TYPE = "SPACY_NE";

	private final Tag tag;
	private final Set<String> synonyms;

	private SpacyNerTag(String... synonyms) {
		tag = new Tag(name(), TAG_TYPE);
		this.synonyms = new HashSet<>(Arrays.stream(synonyms).map(String::toUpperCase).collect(Collectors.toList()));
	}

	public Tag getTag() {
		return tag;
	}

	private boolean is(String key) {
		return tag.getTagValue().equals(key) || isSynonym(key);
	}

	private boolean isSynonym(String str) {
		return synonyms.contains(str);
	}

	public static SpacyNerTag fromString(String str) {
		String key = str.toUpperCase();

		for (SpacyNerTag t : values()) {
			if (t.is(key)) {
				return t;
			}
		}

		return UNKNOWN;
	}

}

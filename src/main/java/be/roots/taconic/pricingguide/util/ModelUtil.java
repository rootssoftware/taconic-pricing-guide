package be.roots.taconic.pricingguide.util;

/*
   This file is part of the Taconic Pricing Guide generator.  This code will
   generate a full featured PDF Pricing Guide by using using iText
   (http://www.itextpdf.com) based on JSON files.

   Copyright (C) 2015  Roots nv
   Authors: Koen Dehaen (koen.dehaen@roots.be)

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   For more information, please contact Roots nv at this address: support@roots.be
 */

import org.apache.commons.lang3.StringUtils;

public class ModelUtil {

    public static class CateGoryAndGender {
        private final String category;
        private final boolean male;

        public CateGoryAndGender(String category, boolean male) {
            this.category = category;
            this.male = male;
        }

        public String getCategory() {
            return category;
        }

        public boolean isMale() {
            return male;
        }

        public boolean isFemale() {
            return !male;
        }

    }

    public static CateGoryAndGender stripGenderFromCategoryCode(String categoryCode) {

        if (StringUtils.isEmpty(categoryCode))
            return null;

        if (categoryCode.contains("-F ")) {
            return new CateGoryAndGender(categoryCode.replaceAll("-F ", " "), false);
        } else if (categoryCode.contains("-M ")) {
            return new CateGoryAndGender(categoryCode.replaceAll("-M ", " "), true);
        } else {
            return null;
        }

    }

    public static String getGenderSpecifcFromCategoryCode(String genderNeutralCategoryCode, boolean male) {

        if (StringUtils.isEmpty(genderNeutralCategoryCode))
            return null;

        if (stripGenderFromCategoryCode(genderNeutralCategoryCode) != null) {
            throw new RuntimeException("CategoryCode " + genderNeutralCategoryCode + " already contains a gender determinator.");
        }

        final String genderCode = male ? "-M" : "-F";
        if (genderNeutralCategoryCode.contains(" ")) {
            return genderNeutralCategoryCode.substring(0, genderNeutralCategoryCode.indexOf(" "))
                    + genderCode
                    + genderNeutralCategoryCode.substring(genderNeutralCategoryCode.indexOf(" "));
        } else {
            return genderNeutralCategoryCode + genderCode;
        }

    }
}

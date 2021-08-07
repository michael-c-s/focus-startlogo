package com.assessbyphone.zambia.Models;

import java.util.ArrayList;


/* master.csv model:
Assessment:P1			Write the letter	Say the word	Say the non-word	What is this?	Read this			Answer the questions	Answer the questions
50413_data_banks.csv	single_letter_1		cvc_word_1		cvc_non_word_1		images_1		comprehension_1		comprehension_q1		comprehension_q4
						single_letter_1		cvc_word_1		cvc_non_word_1						comprehension_q2	comprehension_q5
						single_letter_1		ccvcc_word_1	cvc_non_word_1						comprehension_q3	
						ccvcc_word_2		ccvcc_non_word1				

 */
public class MasterCSVModel {
    public static boolean term1Locked = false;
    public static boolean term2Locked = false;
    public static boolean term3Locked = false;

    public String assessmentName;
    public String dataBankFilename;
    public ArrayList<OneFrame> frames = new ArrayList<OneFrame>();
    public boolean isStory = false;
    public String language = null;

    public boolean isMathAssessment() {
        return assessmentName.toLowerCase().contains("math");
    }

    public enum AgeGroup {
        NotSpecified,
        a56,
        a67,
        a78,
        a89,
        a910,
        a1011,

        // Extend age group to encompass Basic, Intermediate and Advanced levels (namely for the Word Problems in Math Assessments)
        Basic,
        Intermediate,
        Advanced
    }

    public enum WPLevel {
        NotSpecified,
        Basic,
        Intermediate,
        Advanced
    }

    public static class OneFrame {
        public String assessmentName;
        public String question;
        public ArrayList<String> dataBankColumnNames = new ArrayList<String>();
        public boolean isImage = false;
        public boolean isComprehension = false;
        public boolean isMath = false;
        public ArrayList<ComprehensionQuestion> comprehensionQuestions = new ArrayList<ComprehensionQuestion>();
        public ArrayList<String> data = new ArrayList<String>();

        public OneFrame() {
        }
    }

    public static class ComprehensionQuestion {
        public String columnName = "";
        public int lineNumber = 1;

        public ComprehensionQuestion(String name, int lineNumber) {
            this.columnName = name;
            this.lineNumber = lineNumber;
        }
    }
}

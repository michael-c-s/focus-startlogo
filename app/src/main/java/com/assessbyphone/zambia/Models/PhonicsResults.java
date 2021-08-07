package com.assessbyphone.zambia.Models;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;

import java.io.Serializable;

public class PhonicsResults implements Serializable {
    private String phonics_common_plus_vowel_digraph;
    private String phonics_high_frequency_words;
    private String objectId;
    private Integer term;
    private String phonics_cvc_nonwords;
    private String phonics_four_letters_plus_digraph;
    private String phonics_four_letters_plus_blend;
    private String phonics_cvc_words;
    private String phonics_letter_sounds;
    private String phonics_five_letters_plus_blend;
    private String comprehension_oral;
    private String language;
    private Double serialVersionUID;
    private String comprehension_words_read_int;
    private String comprehension_words_read;
    private String phonics_pictures;
    private String comprehension_reading;
    private String upn;
    private String UPN;
    private String age_group;

    public String getPhonics_common_plus_vowel_digraph() {
        return phonics_common_plus_vowel_digraph;
    }

    public void setPhonics_common_plus_vowel_digraph(String phonics_common_plus_vowel_digraph) {
        this.phonics_common_plus_vowel_digraph = phonics_common_plus_vowel_digraph;
    }

    public String getPhonics_high_frequency_words() {
        return phonics_high_frequency_words;
    }

    public void setPhonics_high_frequency_words(String phonics_high_frequency_words) {
        this.phonics_high_frequency_words = phonics_high_frequency_words;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getPhonics_cvc_nonwords() {
        return phonics_cvc_nonwords;
    }

    public void setPhonics_cvc_nonwords(String phonics_cvc_nonwords) {
        this.phonics_cvc_nonwords = phonics_cvc_nonwords;
    }

    public String getPhonics_four_letters_plus_digraph() {
        return phonics_four_letters_plus_digraph;
    }

    public void setPhonics_four_letters_plus_digraph(String phonics_four_letters_plus_digraph) {
        this.phonics_four_letters_plus_digraph = phonics_four_letters_plus_digraph;
    }

    public String getPhonics_four_letters_plus_blend() {
        return phonics_four_letters_plus_blend;
    }

    public void setPhonics_four_letters_plus_blend(String phonics_four_letters_plus_blend) {
        this.phonics_four_letters_plus_blend = phonics_four_letters_plus_blend;
    }

    public String getPhonics_cvc_words() {
        return phonics_cvc_words;
    }

    public void setPhonics_cvc_words(String phonics_cvc_words) {
        this.phonics_cvc_words = phonics_cvc_words;
    }

    public String getPhonics_letter_sounds() {
        return phonics_letter_sounds;
    }

    public void setPhonics_letter_sounds(String phonics_letter_sounds) {
        this.phonics_letter_sounds = phonics_letter_sounds;
    }

    public String getPhonics_five_letters_plus_blend() {
        return phonics_five_letters_plus_blend;
    }

    public void setPhonics_five_letters_plus_blend(String phonics_five_letters_plus_blend) {
        this.phonics_five_letters_plus_blend = phonics_five_letters_plus_blend;
    }

    public String getComprehension_oral() {
        return comprehension_oral;
    }

    public void setComprehension_oral(String comprehension_oral) {
        this.comprehension_oral = comprehension_oral;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setSerialVersionUID(Double serialVersionUID) {
        this.serialVersionUID = serialVersionUID;
    }

    public String getComprehension_words_read_int() {
        if (comprehension_words_read_int == null)
            if (comprehension_words_read == null || comprehension_words_read.equalsIgnoreCase("N"))
                comprehension_words_read = "0";

        return comprehension_words_read_int;
    }

    public String getComprehension_words_read() {
        return comprehension_words_read;
    }

    public void setComprehension_words_read(String comprehension_words_read) {
        this.comprehension_words_read = comprehension_words_read;
    }

    public String getPhonics_pictures() {
        return phonics_pictures;
    }

    public void setPhonics_pictures(String phonics_pictures) {
        this.phonics_pictures = phonics_pictures;
    }

    public String getComprehension_reading() {
        return comprehension_reading;
    }

    public void setComprehension_reading(String comprehension_reading) {
        this.comprehension_reading = comprehension_reading;
    }

    public String getUpn() {
        return upn;
    }

    public void setUpn(String upn) {
        this.upn = upn;
    }

    public String getUPN() {
        return UPN;
    }

    public void setUPN(String UPN) {
        this.UPN = UPN;
    }

    public String getAge_group() {
        return age_group;
    }

    public void setAge_group(String age_group) {
        this.age_group = age_group;
    }

    public void setComprehension_words_read_int(String comprehension_words_read_int) {
        this.comprehension_words_read_int = comprehension_words_read_int;
    }

    public PhonicsResults save() {
        return Backendless.Data.of(PhonicsResults.class).save(this);
    }

    public void saveAsync(AsyncCallback<PhonicsResults> callback) {
        Backendless.Data.of(PhonicsResults.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(PhonicsResults.class).remove(this);
    }

}
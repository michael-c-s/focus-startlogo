package com.assessbyphone.zambia.Models;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MathsResults implements Serializable {
    private String subtract_2_digits_from_2_digits;
    private String identify_greatest_number_to_9999;
    private String divide_2_digit_by_1_digit_numbers_to_20;
    private String counting_in_various_missing_numbers;
    private String counting_in_tens_missing_numbers;
    private String identify_number_to_10;
    private String ownerId;
    private String UPN;
    private String divide_2_digit_by_1_digit_numbers_to_99;
    private String objectId;
    private String wp_subtraction;
    private String counting_in_3s_4s_6s_7s_8s_9s_missing_numbers;
    private Date created;
    private String wp_level;
    private String identify_number_to_999;
    private String counting_objects_to_10;
    private String subtract_numbers_to_10;
    private String wp_division;
    private String counting_in_2s_5s_missing_numbers;
    private String wp_addition;
    private String subtract_2_digit_from_3_digits_including_deci;
    private String multiply_1_digit_numbers;
    private String divide_2_and_3_digit_by_2_digit_numbers;
    private String identify_number_to_9999999;
    private String add_numbers_to_10;
    private String identify_greatest_number_to_50;
    private String counting_in_ones_missing_numbers;
    private String multiply_2_digit_by_2_digit_numbers_to_20;
    private String identify_greatest_number_to_150;
    private Date updated;
    private String divide_2_digit_by_1_digit_numbers_over_20;
    private String multiply_2_digit_by_2_digit_numbers_over_20;
    private String subtract_numbers_to_20;
    private String term;
    private String subtract_1_digit_from_2_digits_to_99;
    private String add_numbers_to_20;
    private String identify_greatest_number_to_1100;
    private String wp_multiplication;
    private String multiply_1_digit_by_2_digit_numbers;
    private String identify_number_to_99;
    private String add_two_2_digit_numbers;
    private String add_two_three_digit_numbers_including_deci;
    private String identify_greatest_number_to_20;
    private String identify_number_to_20;
    private String add_two_and_three_digit_numbers;

    public static MathsResults findById(String id) {
        return Backendless.Data.of(MathsResults.class).findById(id);
    }

    public static void findByIdAsync(String id, AsyncCallback<MathsResults> callback) {
        Backendless.Data.of(MathsResults.class).findById(id, callback);
    }

    public static MathsResults findFirst() {
        return Backendless.Data.of(MathsResults.class).findFirst();
    }

    public static void findFirstAsync(AsyncCallback<MathsResults> callback) {
        Backendless.Data.of(MathsResults.class).findFirst(callback);
    }

    public static MathsResults findLast() {
        return Backendless.Data.of(MathsResults.class).findLast();
    }

    public static void findLastAsync(AsyncCallback<MathsResults> callback) {
        Backendless.Data.of(MathsResults.class).findLast(callback);
    }

    public static List<MathsResults> find(DataQueryBuilder queryBuilder) {
        return Backendless.Data.of(MathsResults.class).find(queryBuilder);
    }

    public static void findAsync(DataQueryBuilder queryBuilder, AsyncCallback<List<MathsResults>> callback) {
        Backendless.Data.of(MathsResults.class).find(queryBuilder, callback);
    }

    public String getSubtract_2_digits_from_2_digits() {
        return subtract_2_digits_from_2_digits;
    }

    public void setSubtract_2_digits_from_2_digits(String subtract_2_digits_from_2_digits) {
        this.subtract_2_digits_from_2_digits = subtract_2_digits_from_2_digits;
    }

    public String getIdentify_greatest_number_to_9999() {
        return identify_greatest_number_to_9999;
    }

    public void setIdentify_greatest_number_to_9999(String identify_greatest_number_to_9999) {
        this.identify_greatest_number_to_9999 = identify_greatest_number_to_9999;
    }

    public String getDivide_2_digit_by_1_digit_numbers_to_20() {
        return divide_2_digit_by_1_digit_numbers_to_20;
    }

    public void setDivide_2_digit_by_1_digit_numbers_to_20(String divide_2_digit_by_1_digit_numbers_to_20) {
        this.divide_2_digit_by_1_digit_numbers_to_20 = divide_2_digit_by_1_digit_numbers_to_20;
    }

    public String getCounting_in_various_missing_numbers() {
        return counting_in_various_missing_numbers;
    }

    public void setCounting_in_various_missing_numbers(String counting_in_various_missing_numbers) {
        this.counting_in_various_missing_numbers = counting_in_various_missing_numbers;
    }

    public String getCounting_in_tens_missing_numbers() {
        return counting_in_tens_missing_numbers;
    }

    public void setCounting_in_tens_missing_numbers(String counting_in_tens_missing_numbers) {
        this.counting_in_tens_missing_numbers = counting_in_tens_missing_numbers;
    }

    public String getIdentify_number_to_10() {
        return identify_number_to_10;
    }

    public void setIdentify_number_to_10(String identify_number_to_10) {
        this.identify_number_to_10 = identify_number_to_10;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getDivide_2_digit_by_1_digit_numbers_to_99() {
        return divide_2_digit_by_1_digit_numbers_to_99;
    }

    public void setDivide_2_digit_by_1_digit_numbers_to_99(String divide_2_digit_by_1_digit_numbers_to_99) {
        this.divide_2_digit_by_1_digit_numbers_to_99 = divide_2_digit_by_1_digit_numbers_to_99;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getUPN() {
        return UPN;
    }

    public void setUPN(String UPN) {
        this.UPN = UPN;
    }

    public String getWp_subtraction() {
        return wp_subtraction;
    }

    public void setWp_subtraction(String wp_subtraction) {
        this.wp_subtraction = wp_subtraction;
    }

    public String getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers() {
        return counting_in_3s_4s_6s_7s_8s_9s_missing_numbers;
    }

    public void setCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers(String counting_in_3s_4s_6s_7s_8s_9s_missing_numbers) {
        this.counting_in_3s_4s_6s_7s_8s_9s_missing_numbers = counting_in_3s_4s_6s_7s_8s_9s_missing_numbers;
    }

    public Date getCreated() {
        return created;
    }

    public String getWp_level() {
        return wp_level;
    }

    public void setWp_level(String wp_level) {
        this.wp_level = wp_level;
    }

    public String getIdentify_number_to_999() {
        return identify_number_to_999;
    }

    public void setIdentify_number_to_999(String identify_number_to_999) {
        this.identify_number_to_999 = identify_number_to_999;
    }

    public String getCounting_objects_to_10() {
        return counting_objects_to_10;
    }

    public void setCounting_objects_to_10(String counting_objects_to_10) {
        this.counting_objects_to_10 = counting_objects_to_10;
    }

    public String getSubtract_numbers_to_10() {
        return subtract_numbers_to_10;
    }

    public void setSubtract_numbers_to_10(String subtract_numbers_to_10) {
        this.subtract_numbers_to_10 = subtract_numbers_to_10;
    }

    public String getWp_division() {
        return wp_division;
    }

    public void setWp_division(String wp_division) {
        this.wp_division = wp_division;
    }

    public String getCounting_in_2s_5s_missing_numbers() {
        return counting_in_2s_5s_missing_numbers;
    }

    public void setCounting_in_2s_5s_missing_numbers(String counting_in_2s_5s_missing_numbers) {
        this.counting_in_2s_5s_missing_numbers = counting_in_2s_5s_missing_numbers;
    }

    public String getWp_addition() {
        return wp_addition;
    }

    public void setWp_addition(String wp_addition) {
        this.wp_addition = wp_addition;
    }

    public String getSubtract_2_digit_from_3_digits_including_deci() {
        return subtract_2_digit_from_3_digits_including_deci;
    }

    public void setSubtract_2_digit_from_3_digits_including_deci(String subtract_2_digit_from_3_digits_including_deci) {
        this.subtract_2_digit_from_3_digits_including_deci = subtract_2_digit_from_3_digits_including_deci;
    }

    public String getMultiply_1_digit_numbers() {
        return multiply_1_digit_numbers;
    }

    public void setMultiply_1_digit_numbers(String multiply_1_digit_numbers) {
        this.multiply_1_digit_numbers = multiply_1_digit_numbers;
    }

    public String getDivide_2_and_3_digit_by_2_digit_numbers() {
        return divide_2_and_3_digit_by_2_digit_numbers;
    }

    public void setDivide_2_and_3_digit_by_2_digit_numbers(String divide_2_and_3_digit_by_2_digit_numbers) {
        this.divide_2_and_3_digit_by_2_digit_numbers = divide_2_and_3_digit_by_2_digit_numbers;
    }

    public String getIdentify_number_to_9999999() {
        return identify_number_to_9999999;
    }

    public void setIdentify_number_to_9999999(String identify_number_to_9999999) {
        this.identify_number_to_9999999 = identify_number_to_9999999;
    }

    public String getAdd_numbers_to_10() {
        return add_numbers_to_10;
    }

    public void setAdd_numbers_to_10(String add_numbers_to_10) {
        this.add_numbers_to_10 = add_numbers_to_10;
    }

    public String getIdentify_greatest_number_to_50() {
        return identify_greatest_number_to_50;
    }

    public void setIdentify_greatest_number_to_50(String identify_greatest_number_to_50) {
        this.identify_greatest_number_to_50 = identify_greatest_number_to_50;
    }

    public String getCounting_in_ones_missing_numbers() {
        return counting_in_ones_missing_numbers;
    }

    public void setCounting_in_ones_missing_numbers(String counting_in_ones_missing_numbers) {
        this.counting_in_ones_missing_numbers = counting_in_ones_missing_numbers;
    }

    public String getMultiply_2_digit_by_2_digit_numbers_to_20() {
        return multiply_2_digit_by_2_digit_numbers_to_20;
    }

    public void setMultiply_2_digit_by_2_digit_numbers_to_20(String multiply_2_digit_by_2_digit_numbers_to_20) {
        this.multiply_2_digit_by_2_digit_numbers_to_20 = multiply_2_digit_by_2_digit_numbers_to_20;
    }

    public String getIdentify_greatest_number_to_150() {
        return identify_greatest_number_to_150;
    }

    public void setIdentify_greatest_number_to_150(String identify_greatest_number_to_150) {
        this.identify_greatest_number_to_150 = identify_greatest_number_to_150;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getDivide_2_digit_by_1_digit_numbers_over_20() {
        return divide_2_digit_by_1_digit_numbers_over_20;
    }

    public void setDivide_2_digit_by_1_digit_numbers_over_20(String divide_2_digit_by_1_digit_numbers_over_20) {
        this.divide_2_digit_by_1_digit_numbers_over_20 = divide_2_digit_by_1_digit_numbers_over_20;
    }

    public String getMultiply_2_digit_by_2_digit_numbers_over_20() {
        return multiply_2_digit_by_2_digit_numbers_over_20;
    }

    public void setMultiply_2_digit_by_2_digit_numbers_over_20(String multiply_2_digit_by_2_digit_numbers_over_20) {
        this.multiply_2_digit_by_2_digit_numbers_over_20 = multiply_2_digit_by_2_digit_numbers_over_20;
    }

    public String getSubtract_numbers_to_20() {
        return subtract_numbers_to_20;
    }

    public void setSubtract_numbers_to_20(String subtract_numbers_to_20) {
        this.subtract_numbers_to_20 = subtract_numbers_to_20;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getSubtract_1_digit_from_2_digits_to_99() {
        return subtract_1_digit_from_2_digits_to_99;
    }

    public void setSubtract_1_digit_from_2_digits_to_99(String subtract_1_digit_from_2_digits_to_99) {
        this.subtract_1_digit_from_2_digits_to_99 = subtract_1_digit_from_2_digits_to_99;
    }

    public String getAdd_numbers_to_20() {
        return add_numbers_to_20;
    }

    public void setAdd_numbers_to_20(String add_numbers_to_20) {
        this.add_numbers_to_20 = add_numbers_to_20;
    }

    public String getIdentify_greatest_number_to_1100() {
        return identify_greatest_number_to_1100;
    }

    public void setIdentify_greatest_number_to_1100(String identify_greatest_number_to_1100) {
        this.identify_greatest_number_to_1100 = identify_greatest_number_to_1100;
    }

    public String getWp_multiplication() {
        return wp_multiplication;
    }

    public void setWp_multiplication(String wp_multiplication) {
        this.wp_multiplication = wp_multiplication;
    }

    public String getMultiply_1_digit_by_2_digit_numbers() {
        return multiply_1_digit_by_2_digit_numbers;
    }

    public void setMultiply_1_digit_by_2_digit_numbers(String multiply_1_digit_by_2_digit_numbers) {
        this.multiply_1_digit_by_2_digit_numbers = multiply_1_digit_by_2_digit_numbers;
    }

    public String getIdentify_number_to_99() {
        return identify_number_to_99;
    }

    public void setIdentify_number_to_99(String identify_number_to_99) {
        this.identify_number_to_99 = identify_number_to_99;
    }

    public String getAdd_two_2_digit_numbers() {
        return add_two_2_digit_numbers;
    }

    public void setAdd_two_2_digit_numbers(String add_two_2_digit_numbers) {
        this.add_two_2_digit_numbers = add_two_2_digit_numbers;
    }

    public String getAdd_two_three_digit_numbers_including_deci() {
        return add_two_three_digit_numbers_including_deci;
    }

    public void setAdd_two_three_digit_numbers_including_deci(String add_two_three_digit_numbers_including_deci) {
        this.add_two_three_digit_numbers_including_deci = add_two_three_digit_numbers_including_deci;
    }

    public String getIdentify_greatest_number_to_20() {
        return identify_greatest_number_to_20;
    }

    public void setIdentify_greatest_number_to_20(String identify_greatest_number_to_20) {
        this.identify_greatest_number_to_20 = identify_greatest_number_to_20;
    }

    public String getIdentify_number_to_20() {
        return identify_number_to_20;
    }

    public void setIdentify_number_to_20(String identify_number_to_20) {
        this.identify_number_to_20 = identify_number_to_20;
    }

    public String getAdd_two_and_three_digit_numbers() {
        return add_two_and_three_digit_numbers;
    }

    public void setAdd_two_and_three_digit_numbers(String add_two_and_three_digit_numbers) {
        this.add_two_and_three_digit_numbers = add_two_and_three_digit_numbers;
    }

    public MathsResults save() {
        return Backendless.Data.of(MathsResults.class).save(this);
    }

    public void saveAsync(AsyncCallback<MathsResults> callback) {
        Backendless.Data.of(MathsResults.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(MathsResults.class).remove(this);
    }

    public void removeAsync(AsyncCallback<Long> callback) {
        Backendless.Data.of(MathsResults.class).remove(this, callback);
    }
}
/**
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is OpenELIS code.
 *
 * Copyright (C) CIRG, University of Washington, Seattle WA.  All Rights Reserved.
 *
 */
package org.openelisglobal.patient.action.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.validator.GenericValidator;
import org.hibernate.validator.constraints.SafeHtml;
import org.openelisglobal.common.formfields.FormFields.Field;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.services.DisplayListService.ListType;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.common.util.validator.CustomDateValidator.DateRelation;
import org.openelisglobal.common.validator.ValidationHelper;
import org.openelisglobal.dictionary.service.DictionaryService;
import org.openelisglobal.dictionary.valueholder.Dictionary;
import org.openelisglobal.organization.service.OrganizationService;
import org.openelisglobal.organization.valueholder.Organization;
import org.openelisglobal.patient.action.IPatientUpdate.PatientUpdateStatus;
import org.openelisglobal.patient.service.PatientTypeService;
import org.openelisglobal.patientidentity.valueholder.PatientIdentity;
import org.openelisglobal.patienttype.valueholder.PatientType;
import org.openelisglobal.sample.form.SamplePatientEntryForm;
import org.openelisglobal.sample.form.SamplePatientEntryForm.SamplePatientEntryBatch;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.validation.annotations.OptionalNotBlank;
import org.openelisglobal.validation.annotations.ValidDate;
import org.openelisglobal.validation.annotations.ValidName;
import org.openelisglobal.validation.constraintvalidator.NameValidator.NameType;

public class PatientManagementInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ValidDate(relative = DateRelation.TODAY, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String currentDate;

    // TODO removable?
    private String patientLastUpdated;
    // TODO removable?
    private String personLastUpdated;

    private PatientUpdateStatus patientUpdateStatus;

    @Pattern(regexp = ValidationHelper.PATIENT_ID_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String patientPK;
    @Pattern(regexp = ValidationHelper.PATIENT_ID_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String STnumber;
    @Pattern(regexp = ValidationHelper.PATIENT_ID_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String subjectNumber;
    @Pattern(regexp = ValidationHelper.PATIENT_ID_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String nationalId;
    @Pattern(regexp = ValidationHelper.PATIENT_ID_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String guid;

    @OptionalNotBlank(formFields = { Field.PatientNameRequired }, groups = {
            SamplePatientEntryForm.SamplePatientEntry.class })
    @ValidName(nameType = NameType.LAST_NAME, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String lastName;
    @OptionalNotBlank(formFields = { Field.PatientNameRequired }, groups = {
            SamplePatientEntryForm.SamplePatientEntry.class })
    @ValidName(nameType = NameType.FIRST_NAME, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String firstName;
    @ValidName(nameType = NameType.FULL_NAME, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String aka;

    @ValidName(nameType = NameType.LAST_NAME, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String mothersName;
    @Size(max = 1)
    private String mothersInitial;

    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String streetAddress;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String city;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String commune;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String addressDepartment;

    @NotBlank(groups = { SamplePatientEntryForm.SamplePatientEntry.class })
    @Pattern(regexp = ValidationHelper.GENDER_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String gender;
    @NotBlank(groups = { SamplePatientEntryForm.SamplePatientEntry.class })
    @Size(max = 3, groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    @Pattern(regexp = "^[0-9]*$", groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String age;
    @ValidDate(relative = DateRelation.PAST, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String birthDateForDisplay = "";

    @Pattern(regexp = ValidationHelper.ID_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String patientType = "";

    // for display
    private static List<PatientType> patientTypes;

    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String insuranceNumber;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String occupation;
    @Pattern(regexp = ValidationHelper.PHONE_REGEX, groups = { SamplePatientEntryForm.SamplePatientEntry.class,
            SamplePatientEntryBatch.class })
    private String primaryPhone;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String healthRegion;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String education;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String maritialStatus;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String nationality;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String healthDistrict;
    @SafeHtml(groups = { SamplePatientEntryForm.SamplePatientEntry.class, SamplePatientEntryBatch.class })
    private String otherNationality;

    // for display
    private static List<Dictionary> addressDepartments;

    private boolean readOnly = false;

    private List<PatientIdentity> patientIdentities;

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getPatientLastUpdated() {
        return patientLastUpdated;
    }

    public void setPatientLastUpdated(String patientLastUpdated) {
        this.patientLastUpdated = patientLastUpdated;
    }

    public String getPersonLastUpdated() {
        return personLastUpdated;
    }

    public void setPersonLastUpdated(String personLastUpdated) {
        this.personLastUpdated = personLastUpdated;
    }

    public String getPatientPK() {
        return patientPK;
    }

    public void setPatientPK(String patientPK) {
        this.patientPK = patientPK;
    }

    public String getSTnumber() {
        return STnumber;
    }

    public void setSTnumber(String sTnumber) {
        STnumber = sTnumber;
    }

    public String getSubjectNumber() {
        return subjectNumber;
    }

    public void setSubjectNumber(String subjectNumber) {
        this.subjectNumber = subjectNumber;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getAka() {
        return aka;
    }

    public void setAka(String aka) {
        this.aka = aka;
    }

    public String getMothersName() {
        return mothersName;
    }

    public void setMothersName(String mothersName) {
        this.mothersName = mothersName;
    }

    public String getMothersInitial() {
        return mothersInitial;
    }

    public void setMothersInitial(String mothersInitial) {
        this.mothersInitial = mothersInitial;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getBirthDateForDisplay() {
        return birthDateForDisplay;
    }

    public void setBirthDateForDisplay(String birthDateForDisplay) {
        this.birthDateForDisplay = birthDateForDisplay;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPatientType() {
        return patientType;
    }

    public void setPatientType(String patientType) {
        this.patientType = patientType;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public List<IdValuePair> getGenders() {
        return DisplayListService.getInstance().getList(ListType.GENDERS);
    }

    public void setPatientTypes(List<PatientType> patientTypes) {
        PatientManagementInfo.patientTypes = patientTypes;
    }

    @SuppressWarnings("unchecked")
    public List<PatientType> getPatientTypes() {
        if (patientTypes == null) {
            patientTypes = SpringContext.getBean(PatientTypeService.class).getAllPatientTypes();
        }
        return patientTypes;
    }

    public void setAddressDepartment(String addressDepartment) {
        this.addressDepartment = addressDepartment;
    }

    public String getAddressDepartment() {
        return addressDepartment;
    }

    public List<Dictionary> getAddressDepartments() {
        if (addressDepartments == null) {
            addressDepartments = SpringContext.getBean(DictionaryService.class)
                    .getDictionaryEntrysByCategoryAbbreviation("description", "haitiDepartment", true);
        }

        return addressDepartments;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public List<IdValuePair> getHealthRegions() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_HEALTH_REGIONS);
    }

    public List<IdValuePair> getHealthDistricts() {
        List<IdValuePair> districtsList = new ArrayList<>();
        if (!GenericValidator.isBlankOrNull(healthRegion)) {
            List<Organization> districts = SpringContext.getBean(OrganizationService.class)
                    .getOrganizationsByParentId(healthRegion);
            for (Organization district : districts) {
                districtsList.add(new IdValuePair(district.getId(), district.getOrganizationName()));
            }
        }
        return districtsList;
    }

    public String getHealthRegion() {
        return healthRegion;
    }

    public void setHealthRegion(String healthRegion) {
        this.healthRegion = healthRegion;
    }

    public String getHealthDistrict() {
        return healthDistrict;
    }

    public void setHealthDistrict(String healthDistrict) {
        this.healthDistrict = healthDistrict;
    }

    public List<IdValuePair> getEducationList() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_EDUCATION);
    }

    public List<IdValuePair> getMaritialList() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_MARITAL_STATUS);
    }

    public List<IdValuePair> getNationalityList() {
        return DisplayListService.getInstance().getList(ListType.PATIENT_NATIONALITY);
    }

    public String getOtherNationality() {
        return otherNationality;
    }

    public void setOtherNationality(String otherNationality) {
        this.otherNationality = otherNationality;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getMaritialStatus() {
        return maritialStatus;
    }

    public void setMaritialStatus(String maritialStatus) {
        this.maritialStatus = maritialStatus;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String naionality) {
        nationality = naionality;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public PatientUpdateStatus getPatientUpdateStatus() {
        return patientUpdateStatus;
    }

    public void setPatientUpdateStatus(PatientUpdateStatus patientUpdateStatus) {
        this.patientUpdateStatus = patientUpdateStatus;
    }

    public List<PatientIdentity> getPatientIdentities() {
        return patientIdentities;
    }

    public void setPatientIdentities(List<PatientIdentity> patientIdentities) {
        this.patientIdentities = patientIdentities;
    }
}

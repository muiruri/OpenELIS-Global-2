package org.openelisglobal.testconfiguration.form;

import java.util.List;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.SafeHtml;
import org.openelisglobal.common.form.BaseForm;
import org.openelisglobal.common.util.IdValuePair;

public class TestSectionCreateForm extends BaseForm {
    // for display
    private List existingTestUnitList;

    // for display
    private List inactiveTestUnitList;

    // for display
    private String existingEnglishNames;

    // for display
    private String existingFrenchNames;

    @NotBlank
    @SafeHtml
    private String testUnitEnglishName;

    @NotBlank
    @SafeHtml
    private String testUnitFrenchName;

    public TestSectionCreateForm() {
        setFormName("testSectionCreateForm");
    }

    public List getExistingTestUnitList() {
        return existingTestUnitList;
    }

    public void setExistingTestUnitList(List<IdValuePair> existingTestUnitList) {
        this.existingTestUnitList = existingTestUnitList;
    }

    public List getInactiveTestUnitList() {
        return inactiveTestUnitList;
    }

    public void setInactiveTestUnitList(List inactiveTestUnitList) {
        this.inactiveTestUnitList = inactiveTestUnitList;
    }

    public String getExistingEnglishNames() {
        return existingEnglishNames;
    }

    public void setExistingEnglishNames(String existingEnglishNames) {
        this.existingEnglishNames = existingEnglishNames;
    }

    public String getExistingFrenchNames() {
        return existingFrenchNames;
    }

    public void setExistingFrenchNames(String existingFrenchNames) {
        this.existingFrenchNames = existingFrenchNames;
    }

    public String getTestUnitEnglishName() {
        return testUnitEnglishName;
    }

    public void setTestUnitEnglishName(String testUnitEnglishName) {
        this.testUnitEnglishName = testUnitEnglishName;
    }

    public String getTestUnitFrenchName() {
        return testUnitFrenchName;
    }

    public void setTestUnitFrenchName(String testUnitFrenchName) {
        this.testUnitFrenchName = testUnitFrenchName;
    }
}

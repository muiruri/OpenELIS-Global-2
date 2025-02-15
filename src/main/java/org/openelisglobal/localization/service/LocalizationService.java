package org.openelisglobal.localization.service;

import java.util.List;
import java.util.Locale;

import org.openelisglobal.common.service.BaseObjectService;
import org.openelisglobal.localization.valueholder.Localization;

public interface LocalizationService extends BaseObjectService<Localization, String> {

    @Override
    String insert(Localization localization);

    boolean languageChanged(Localization localization, Localization oldLocalization);

    void updateTestNames(Localization name, Localization reportingName);

    String getCurrentLocale();

    String getLocalizedValueById(String id);

    List<Locale> getAllActiveLocales();

}

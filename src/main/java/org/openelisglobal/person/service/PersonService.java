package org.openelisglobal.person.service;

import java.util.List;
import java.util.Map;

import org.openelisglobal.common.service.BaseObjectService;
import org.openelisglobal.person.valueholder.Person;

public interface PersonService extends BaseObjectService<Person, String> {
    void getData(Person person);

    List getNextPersonRecord(String id);

    List getPreviousPersonRecord(String id);

    Person getPersonByLastName(String lastName);

    List getPageOfPersons(int startingRecNo);

    List getAllPersons();

    Person getPersonById(String personId);

    String getFirstName(Person person);

    String getLastName(Person person);

    String getLastFirstName(Person person);

    Map<String, String> getAddressComponents(Person person);

    String getPhone(Person person);

    String getWorkPhone(Person person);

    String getCellPhone(Person person);

    String getFax(Person person);

    String getEmail(Person person);

}

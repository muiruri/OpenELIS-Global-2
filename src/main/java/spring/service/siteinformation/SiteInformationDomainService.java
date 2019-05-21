package spring.service.siteinformation;

import spring.service.common.BaseObjectService;
import us.mn.state.health.lims.siteinformation.valueholder.SiteInformationDomain;

public interface SiteInformationDomainService extends BaseObjectService<SiteInformationDomain> {

	SiteInformationDomain getByName(String name);
}

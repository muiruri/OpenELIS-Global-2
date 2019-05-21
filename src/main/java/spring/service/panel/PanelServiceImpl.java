package spring.service.panel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import spring.service.common.BaseObjectServiceImpl;
import us.mn.state.health.lims.panel.dao.PanelDAO;
import us.mn.state.health.lims.panel.valueholder.Panel;

@Service
public class PanelServiceImpl extends BaseObjectServiceImpl<Panel> implements PanelService {
  @Autowired
  protected PanelDAO baseObjectDAO;

  PanelServiceImpl() {
    super(Panel.class);
  }

  @Override
  protected PanelDAO getBaseObjectDAO() {
    return baseObjectDAO;}
}

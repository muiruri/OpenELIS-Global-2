package us.mn.state.health.lims.datasubmission.daoimpl;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import us.mn.state.health.lims.audittrail.dao.AuditTrailDAO;
import us.mn.state.health.lims.audittrail.daoimpl.AuditTrailDAOImpl;
import us.mn.state.health.lims.common.action.IActionConstants;
import us.mn.state.health.lims.common.daoimpl.BaseDAOImpl;
import us.mn.state.health.lims.common.exception.LIMSRuntimeException;
import us.mn.state.health.lims.common.log.LogEvent;
import us.mn.state.health.lims.datasubmission.dao.TypeOfDataIndicatorDAO;
import us.mn.state.health.lims.datasubmission.valueholder.TypeOfDataIndicator;
import us.mn.state.health.lims.hibernate.HibernateUtil;

@Component
@Transactional 
public class TypeOfDataIndicatorDAOImpl extends BaseDAOImpl<TypeOfDataIndicator> implements TypeOfDataIndicatorDAO {

	public TypeOfDataIndicatorDAOImpl() {
		super(TypeOfDataIndicator.class);
	}

	@Override
	public void getData(TypeOfDataIndicator typeOfIndicator) throws LIMSRuntimeException {
		try {
			TypeOfDataIndicator typeOfIndicatorClone = (TypeOfDataIndicator) sessionFactory.getCurrentSession()
					.get(TypeOfDataIndicator.class, typeOfIndicator.getId());
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old
			if (typeOfIndicatorClone != null) {
				PropertyUtils.copyProperties(typeOfIndicator, typeOfIndicatorClone);
			} else {
				typeOfIndicator.setId(null);
			}
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("TypeOfDataIndicatorDAOImpl", "getData()", e.toString());
			throw new LIMSRuntimeException("Error in TypeOfDataIndicator getData()", e);
		}
	}

	@Override
	public List<TypeOfDataIndicator> getAllTypeOfDataIndicator() throws LIMSRuntimeException {
		List<TypeOfDataIndicator> list;
		try {
			String sql = "from TypeOfDataIndicator";
			org.hibernate.Query query = sessionFactory.getCurrentSession().createQuery(sql);
			list = query.list();
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old
		} catch (Exception e) {
			LogEvent.logError("TypeOfDataIndicator", "getAllTypeOfDataIndicator()", e.toString());
			throw new LIMSRuntimeException("Error in TypeOfDataIndicator getAllTypeOfDataIndicator()", e);
		}

		return list;
	}

	@Override
	public TypeOfDataIndicator getTypeOfDataIndicator(String id) throws LIMSRuntimeException {
		try {
			TypeOfDataIndicator dataValue = (TypeOfDataIndicator) sessionFactory.getCurrentSession()
					.get(TypeOfDataIndicator.class, id);
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old
			return dataValue;
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("TypeOfDataIndicatorDAOImpl", "getData()", e.toString());
			throw new LIMSRuntimeException("Error in TypeOfDataIndicator getData()", e);
		}
	}

	@Override
	public boolean insertData(TypeOfDataIndicator typeOfIndicator) throws LIMSRuntimeException {

		try {
			String id = (String) sessionFactory.getCurrentSession().save(typeOfIndicator);
			typeOfIndicator.setId(id);

			AuditTrailDAO auditDAO = new AuditTrailDAOImpl();
			String sysUserId = typeOfIndicator.getSysUserId();
			String tableName = "TYPE_OF_DATA_INDICATOR";
			auditDAO.saveNewHistory(typeOfIndicator, sysUserId, tableName);

			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old

		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("TypeOfDataIndicatorDAOImpl", "insertData()", e.toString());
			throw new LIMSRuntimeException("Error in TypeOfDataIndicator insertData()", e);
		}

		return true;
	}

	@Override
	public void updateData(TypeOfDataIndicator typeOfIndicator) throws LIMSRuntimeException {

		TypeOfDataIndicator oldData = getTypeOfDataIndicator(typeOfIndicator.getId());

		try {
			AuditTrailDAO auditDAO = new AuditTrailDAOImpl();
			String sysUserId = typeOfIndicator.getSysUserId();
			String event = IActionConstants.AUDIT_TRAIL_UPDATE;
			String tableName = "TYPE_OF_DATA_INDICATOR";
			auditDAO.saveHistory(typeOfIndicator, oldData, sysUserId, event, tableName);
		} catch (Exception e) {
			LogEvent.logError("DataValueDAOImpl", "AuditTrail updateData()", e.toString());
			throw new LIMSRuntimeException("Error in TypeOfDataIndicator AuditTrail updateData()", e);
		}

		try {
			sessionFactory.getCurrentSession().merge(typeOfIndicator);
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old
			// sessionFactory.getCurrentSession().evict // CSL remove old(typeOfIndicator);
			// sessionFactory.getCurrentSession().refresh // CSL remove old(typeOfIndicator);
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("TypeOfDataIndicatorDAOImpl", "updateData()", e.toString());
			throw new LIMSRuntimeException("Error in DataValue updateData()", e);
		}
	}

	@Override
	public List getNextRecord(String id, String table, Class clazz) throws LIMSRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getPreviousRecord(String id, String table, Class clazz) throws LIMSRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getTotalCount(String table, Class clazz) throws LIMSRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

}

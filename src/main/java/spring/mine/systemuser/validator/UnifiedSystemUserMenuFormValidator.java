package spring.mine.systemuser.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import spring.mine.systemuser.form.UnifiedSystemUserMenuForm;

@Component
public class UnifiedSystemUserMenuFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UnifiedSystemUserMenuForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UnifiedSystemUserMenuForm form = (UnifiedSystemUserMenuForm) target;
	}

}

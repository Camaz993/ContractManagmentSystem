package contracts.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import contracts.domain.Expired;
import contracts.domain.Operative;
import contracts.repository.AccountRepository;
import contracts.repository.ExpiredRepository;


@Service
public class ExpiredService implements IExpiredService{
	
	@Autowired
	ExpiredRepository expiredRepository;
	
	@Autowired
	AccountRepository accountRepository;
	
	public void setExpiredRepository(ExpiredRepository expiredRepository) {
		this.expiredRepository = expiredRepository;
	}
	
	@Override
	public void addExpired(Expired newExpired) {
		
		try
		{
			expiredRepository.save(newExpired);
		} catch(javax.validation.ConstraintViolationException e)
		{ 
			throw new IllegalArgumentException(e.getConstraintViolations().iterator().next().getMessage());
		}catch (Exception e2)
		{
			e2.printStackTrace();
		}
		
	}
	
	@Override
	public Expired update(Expired newExpired) {	
		return expiredRepository.save(newExpired);
	}
	
	
}

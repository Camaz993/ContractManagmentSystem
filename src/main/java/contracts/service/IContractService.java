/** 
 * The contract interface for the service class
 * @author Alice, Caleb, Laurie, Natalie, Poppy
 */
package contracts.service;

import java.util.List;
import java.util.Optional;

import contracts.domain.Contract;
import contracts.domain.User;

public interface IContractService {
	
	public void addContract(Contract newContract);
	
	public Optional<Contract> findContract(Integer id);

	public List<Contract> getAllContracts();
	
	public List<Contract> getContractsShortList();
	
	public Optional<User> findById(Integer id);
	
	public List<Contract> searchContracts(String search);
	
	public Contract update(Contract contract);

	public List<User> getAllUsers();
	
	public void archiveContract(Contract archivedContract);
	
	public List<Contract> getArchivedContracts();

	public List<Contract> findAllByOrderByIdAsc();
	
	public List<Contract> getUnarchivedContracts();
	
	public List<Contract> getContractsByUser(Integer userid);
	
	public List<Contract> getNullUserContracts();
	
	public List<Contract> getFavouritedContracts(Integer userid);
	
	public Integer findNewestContract();
	
	public void unfavouriteContract(Integer requestid, Integer userid);
	
	public Boolean checkFavourited(Integer requestid, Integer userid);
	
	List<Contract> getAllExceptCurrent(Integer requestid);

	public Object getRelatedContracts(Integer requestid);

	public void unrelateContract(Integer requestid, Integer requestid2);
	
	public List<Contract> getContractsSorted();
	
	public List<Contract> getContractsSortedParty();
}

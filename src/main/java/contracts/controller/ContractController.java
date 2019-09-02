package contracts.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import contracts.domain.contract;
import contracts.domain.status;
import contracts.domain.user;
import contracts.service.IContractService;

@Controller
public class ContractController {
	
	@Autowired
	private IContractService contractService;
	
	@GetMapping("/add_contracts")
    public String showSignUpForm(Model model) {
		model.addAttribute("contract", new contract());
        return "add_contracts";
    }
	
	@PostMapping("/api/contracts")
	public ResponseEntity<String> addContract(@RequestParam Integer user, @RequestParam String agreement_title, @RequestParam String agreement_type,
			@RequestParam String description, @RequestParam String agreement_location, @RequestParam String language, @RequestParam String region, @RequestParam String related_agreements)
	{
		contract contract = new contract();
		user userFind = contractService.findById(user).orElse(new user());
		contract.setUserid(userFind);
		contract.setAgreement_title(agreement_title);
		contract.setAgreement_type(agreement_type);
		contract.setDescription(description);
		contract.setAgreement_location(agreement_location);
		contract.setLanguage(language);
		contract.setRegion(region);
		contract.setRelated_agreements(related_agreements);
		
		contractService.addContract(contract);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", "/");
		return new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
	}
	
	@GetMapping("/search_contracts")
	public String getAllContracts(Model model) {
		model.addAttribute("contracts", contractService.getAllContracts());
		return "search_contracts";
	}
	
	@PostMapping("/api/contracts/search")
	public List<contract> searchContracts(@RequestParam String search)
	{
		return contractService.searchContracts(search);
	}
	
	@PostMapping("/api/contracts/search/location")
	public List<contract> searchLocation(@RequestParam String search)
	{
		return contractService.searchLocation(search);
	}
	
	@PostMapping("/api/contracts/search/type")
	public List<contract> searchContractType(@RequestParam String search)
	{
		return contractService.searchContractType(search);
	}

}

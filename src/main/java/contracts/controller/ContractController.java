package contracts.controller;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.io.Files;

import contracts.domain.Audit;
import contracts.domain.Contract;
import contracts.domain.Expired;
import contracts.domain.Favourited;
import contracts.domain.InNegotiation;
import contracts.domain.Operative;
import contracts.domain.Status;
import contracts.domain.StatusLink;
import contracts.domain.User;
import contracts.repository.ContractRepository;
import contracts.repository.CurrentRepository;
import contracts.repository.ExpiredRepository;
import contracts.repository.InNegotiationRepository;
import contracts.repository.OperativeRepository;
import contracts.repository.StatusLinkRepository;
import contracts.service.AuditService;
import contracts.service.CurrentService;
import contracts.service.IAccountService;
import contracts.service.IContractService;
import contracts.service.IExpiredService;
import contracts.service.IFavouritedService;
import contracts.service.IInNegotiationService;
import contracts.service.IOperativeService;
import contracts.service.IStatusLinkService;

@Controller
public class ContractController {
	
	@Autowired
	private AuditService auditService;
	
	@Autowired
	private IContractService contractService;
	
	@Autowired
	private IAccountService accountService;
	
	@Autowired
	private IOperativeService operativeService;
	
	@Autowired
	private IInNegotiationService in_negotiationService;
	
	@Autowired
	private IExpiredService expiredService;
	
	@Autowired
	private IStatusLinkService statuslinkService;
	
	@Autowired
	private IFavouritedService favouritedService;
	
	@Autowired
	private ContractRepository repo;
	
	@Autowired
	private InNegotiationRepository negRepo;
	
	@Autowired
	private OperativeRepository opRepo;
	
	@Autowired
	private ExpiredRepository exRepo;
	
	@Autowired
	private StatusLinkRepository slRepo;

    @Autowired
	private CurrentService currentService;
	
	@Autowired
	private CurrentRepository currentRepository;

	
	@GetMapping("/add_contracts")
    public String showSignUpForm(Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		model.addAttribute("contract", new Contract());
		model.addAttribute("status", new Status());
		List <User> users = contractService.getAllUsers();
		model.addAttribute("users", users);
        return "add_contracts";
    }
	
	@GetMapping("/add_status")
	public String showStatusForm(Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		Integer requestid = contractService.findNewestContract();
		model.addAttribute("requestid", requestid);
		model.addAttribute("in_negotiation", new InNegotiation());
		model.addAttribute("operative", new Operative());
		model.addAttribute("expired", new Expired());
		repo.findById(requestid).ifPresent(contract->model.addAttribute("selectedContract", contract));
		return "add_status";
	}
	
	//add a new contract to the database
	@PostMapping("/api/contracts")
	public String addContract(@Valid @ModelAttribute(name="contract") Contract contract, BindingResult br, Model model)
	{
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		List <User> users = contractService.getAllUsers();
		model.addAttribute("users", users);
		if(br.hasErrors()) {
		return "add_contracts";
		}
		contract.setArchived("F");
		Date timeNow = new Date(Calendar.getInstance().getTimeInMillis());
		contract.setDate_updated(timeNow);
		contractService.addContract(contract);
		auditService.addAuditDetails(contract);
		return "redirect:/add_status";
	}
	
	//add an in negotiation status to the database, along with null operative and expired status'
	@PostMapping("/api/in_negotiation")
	public String add_in_negotiation(@ModelAttribute(name="in_negotiation") InNegotiation in_negotiation, RedirectAttributes redirectAttributes, Model model)
	{
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		Integer requestid = contractService.findNewestContract();
		in_negotiation.setRequestId(requestid);
		in_negotiationService.addInNegotiation(in_negotiation);
		Operative op = new Operative();
		op.setRequestId(requestid);
		operativeService.addOperative(op);
		Expired ex = new Expired();
		ex.setRequestId(requestid);
		expiredService.addExpired(ex);
		StatusLink statuslink = new StatusLink(requestid, "in_negotiation");
		statuslinkService.addStatusLink(statuslink);
		redirectAttributes.addFlashAttribute("message", "Contract successfully added");
		return "redirect:/view_details/" + requestid;
	}
	
	//add an operative status to the db, along with null in negotiation and expired status'
	@PostMapping("/api/operative")
	public String add_operative(@ModelAttribute(name="operative") Operative operative, RedirectAttributes redirectAttributes, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		Integer requestid = contractService.findNewestContract();
		operative.setRequestId(requestid);		
		operativeService.addOperative(operative);
		Expired ex = new Expired();
		ex.setRequestId(requestid);
		expiredService.addExpired(ex);
		InNegotiation neg = new InNegotiation();
		neg.setRequestId(requestid);
		in_negotiationService.addInNegotiation(neg);
		StatusLink statuslink = new StatusLink(requestid, "operative");
		statuslinkService.addStatusLink(statuslink);
		redirectAttributes.addFlashAttribute("message", "Contract successfully added");
		return "redirect:/view_details/" + requestid;
	}
	
	//add an expired status in the db, along with in negotiation and operative status'
	@PostMapping("/api/expired")
	public String add_expired(@ModelAttribute(name="expired") Expired expired, RedirectAttributes redirectAttributes, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		Integer requestid = contractService.findNewestContract();
		expired.setRequestId(requestid);
		expiredService.addExpired(expired);
		InNegotiation neg = new InNegotiation();
		neg.setRequestId(requestid);
		in_negotiationService.addInNegotiation(neg);
		Operative op = new Operative();
		op.setRequestId(requestid);
		operativeService.addOperative(op);
		StatusLink statuslink = new StatusLink(requestid, "expired");
		statuslinkService.addStatusLink(statuslink);
		redirectAttributes.addFlashAttribute("message", "Contract successfully added");
		return "redirect:/view_details/" + requestid;
	}
	
	//Method to bring up search results and checks if user has item favourited or not.
	//If they have the item favourited, the button dynamically updates to unfavourited.
	@GetMapping("/search_contracts")
	public String getAllContracts(Model model) {
		List<Contract> allContracts = contractService.getAllContracts();
		List favStatus = new ArrayList<>();
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails)principal).getUsername();
		User user = accountService.findUser(username);
		for (int i = 0; i < allContracts.size(); i++) {
			if (contractService.checkFavourited(allContracts.get(i).getRequestid(), user.getUserid())) {
				favStatus.add("favourited");
			}
			else {
				favStatus.add("unfavourited");
			}
		}
		

		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		model.addAttribute("contracts", contractService.getAllContracts());
		model.addAttribute("favstatus", favStatus);
		return "search_contracts";
	}
	
	
	@GetMapping("/")
	public String mostRecent(Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		model.addAttribute("contracts", contractService.getAllContracts());
		model.addAttribute("contracts2", contractService.getNullUserContracts());
		return "index";
	}
	
	//assign a specific user to a contract
	@PostMapping("/assign/{requestid}")
	public String assignUser(@PathVariable("requestid") int requestid, Model model) {
		Contract foundContract = contractService.findContract(requestid).orElse(new Contract());
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails)principal).getUsername();
		User user = accountService.findUser(username);
		foundContract.setUserid(user);
		contractService.update(foundContract);
		return "redirect:/my_contracts";
	}
	
	/*
	@PostMapping("/api/contracts/search")
	public List<Contract> searchContracts(@RequestParam String search)
	{
		return contractService.searchContracts(search);
	}
	@GetMapping("/contracts/search")
	public String searchContracts(@RequestParam String search, Model model)
	{
		model.addAttribute("contracts/search", contractService.searchContracts(search));
		return "search_contracts";
	}*/
	
	@GetMapping("/contracts/search")
	public List<Contract> searchContracts(@PathVariable("search") String search, Model model, ModelMap map)
	{
		model.addAttribute("search", search);
		//map.put("search", search);
		return contractService.searchContracts(search);
		//return "search";
	}
	
	@PostMapping("/api/contracts/search/location")
	public List<Contract> searchLocation(@RequestParam String search)
	{
		return contractService.searchLocation(search);
	}
	
	@PostMapping("/api/contracts/search/type")
	public List<Contract> searchContractType(@RequestParam String search)
	{
		return contractService.searchContractType(search);
	}

	@GetMapping("/api/contracts")
	public List<Contract> allContracts()
	{
		return contractService.getAllContracts();
	}
	
	@GetMapping("/contract/{requestid}")
	@ResponseBody 
	public Optional<Contract> getContract(@PathVariable("requestid") int requestid) {
		return repo.findById(requestid);
	}
	
	@GetMapping("/view_details/{requestid}")
	public String selectedContract(@PathVariable("requestid") int requestid, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		repo.findById(requestid).ifPresent(o->model.addAttribute("selectedContract", o));
		slRepo.findById(requestid).ifPresent(o->model.addAttribute("status", o));
		opRepo.findById(requestid).ifPresent(o->model.addAttribute("operative", o));
		exRepo.findById(requestid).ifPresent(o->model.addAttribute("expired", o));
		negRepo.findById(requestid).ifPresent(o->model.addAttribute("in_negotiation", o));
		return "view_details";
	}
	
	//get the contract object from the db to update
	@GetMapping("/update_details/{requestid}")
	public String updateContractForm(@PathVariable("requestid") int requestid, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		repo.findById(requestid).ifPresent(contract->model.addAttribute("contract", contract));
		negRepo.findById(requestid).ifPresent(in_negotiation->model.addAttribute("in_negotiation", in_negotiation));
		List <User> users = contractService.getAllUsers();
		model.addAttribute("users", users);
		return "update_details";
	}
	
	//update a contract and store the results of the changes in the audit table
	@PostMapping("/api/updates")
	public String updateDetails(@Valid @ModelAttribute(name="contract") Contract contract, BindingResult br, Model model)
	{	
	Integer i = currentService.getCurrent();
	currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
	String fieldUpdatedList = "";
	String fieldBeforeList = "";
	String fieldAfterList = "";
	Contract foundContract = contractService.findContract(contract.getRequestid()).orElse(new Contract());
	Audit blank = new Audit();
	if (!foundContract.getUser().equals(contract.getUser())) {
		fieldBeforeList += ((String.valueOf((foundContract.getUser().getUserid()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getUser().getUserid()))))+ (", ");
		fieldUpdatedList += ("userid") + (", ");
	}
	if (!foundContract.getAgreement_title().equals(contract.getAgreement_title())) {
		fieldBeforeList += ((String.valueOf((foundContract.getAgreement_title()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getAgreement_title()))))+ (", ");
		fieldUpdatedList += ("agreement_title") + (", ");
	}
	if (!foundContract.getAgreement_type().equals(contract.getAgreement_type())) {
		fieldBeforeList += ((String.valueOf((foundContract.getAgreement_type()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getAgreement_type()))))+ (", ");
		fieldUpdatedList += ("agreement_type") + (", ");
	}
	if (!foundContract.getDescription().equals(contract.getDescription())) {
		fieldBeforeList += ((String.valueOf((foundContract.getDescription()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getDescription()))))+ (", ");
		fieldUpdatedList += ("description") + (", ");
	}
	if (!foundContract.getAgreement_location().equals(contract.getAgreement_location())) {
		fieldBeforeList += ((String.valueOf((foundContract.getAgreement_location()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getAgreement_location()))))+ (", ");
		fieldUpdatedList += ("agreement_location") + (", ");
	}
	if (!foundContract.getBusinessname().equals(contract.getBusinessname())) {
		fieldBeforeList += ((String.valueOf((foundContract.getBusinessname()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getBusinessname()))))+ (", ");
		fieldUpdatedList += ("businessname") + (", ");
	}
	if (!foundContract.getClientname().equals(contract.getClientname())) {
		fieldBeforeList += ((String.valueOf((foundContract.getClientname()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getClientname()))))+ (", ");
		fieldUpdatedList += ("clientname") + (", ");
	}
	if (!foundContract.getAddress().equals(contract.getAddress())) {
		fieldBeforeList += ((String.valueOf((foundContract.getAddress()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getAddress()))))+ (", ");
		fieldUpdatedList += ("address") + (", ");
	}
	if (!foundContract.getPhone().equals(contract.getPhone())) {
		fieldBeforeList += ((String.valueOf((foundContract.getPhone()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getPhone()))))+ (", ");
		fieldUpdatedList += ("phone") + (", ");
	}
	if (!foundContract.getEmail().equals(contract.getEmail())) {
		fieldBeforeList += ((String.valueOf((foundContract.getEmail()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getEmail()))))+ (", ");
		fieldUpdatedList += ("email") + (", ");
	}
	if (!foundContract.getFax().equals(contract.getFax())) {
		fieldBeforeList += ((String.valueOf((foundContract.getFax()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getFax()))))+ (", ");
		fieldUpdatedList += ("fax") + (", ");
	}
	if(!foundContract.getLanguage().equals(contract.getLanguage())) {
		fieldBeforeList += ((String.valueOf((foundContract.getLanguage()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getLanguage()))))+ (", ");
		fieldUpdatedList += ("language") + (", ");
	}
	if (!foundContract.getRegion().equals(contract.getRegion())) {
		fieldBeforeList += ((String.valueOf((foundContract.getRegion()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getRegion()))))+ (", ");
		fieldUpdatedList += ("region") + (", ");
	}
	if (!foundContract.getRelated_agreements().equals(contract.getRelated_agreements())) {
		fieldBeforeList += ((String.valueOf((foundContract.getRelated_agreements()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getRelated_agreements()))))+ (", ");
		fieldUpdatedList += ("related_agreements") + (", ");
	}
		Date timeNow = new Date(Calendar.getInstance().getTimeInMillis());
		contract.setDate_updated(timeNow);
		blank.setField_after(fieldAfterList);
		blank.setField_before(fieldBeforeList);
		blank.setField_updated(fieldUpdatedList);
		blank.setUserid(contract.getUser());
		blank.setRequestedid(contract);
		blank.setDate(contract.getDate_updated());
		contractService.update(contract);
		auditService.addAudit(blank);
		return "redirect:/update_status/" + contract.getRequestid();
	}
	
	@GetMapping("/update_status/{requestid}")
	public String updateStatus(@PathVariable("requestid") int requestid, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		negRepo.findById(requestid).ifPresent(in_negotiation->model.addAttribute("in_negotiation", in_negotiation));
		opRepo.findById(requestid).ifPresent(operative->model.addAttribute("operative", operative));
		exRepo.findById(requestid).ifPresent(expired->model.addAttribute("expired", expired));
		repo.findById(requestid).ifPresent(o->model.addAttribute("selectedContract", o));
		return "update_status";	
	}
	
	//updates an in negotiation status 
	@PostMapping("/api/update/in_negotiation")
	public String updateInNegotiation(@ModelAttribute(name="in_negotiation") InNegotiation in_negotiation, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		in_negotiationService.update(in_negotiation);
		Integer requestid = in_negotiation.getRequestId();
		StatusLink stat = new StatusLink(requestid, "in_negotiation");
		statuslinkService.update(stat);
		return "redirect:/";
	}
	
	//updates an operative status
	@PostMapping("/api/update/operative")
	public String updateOperative(@ModelAttribute(name="operative") Operative operative, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		operativeService.update(operative);
		Integer requestid = operative.getRequestId();
		StatusLink stat = new StatusLink(requestid, "operative");
		statuslinkService.update(stat);
		return "redirect:/search_contracts";
	}
	
	//updates an expired status
	@PostMapping("/api/update/expired")
	public String updateExpired(@ModelAttribute(name="expired") Expired expired, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		expiredService.update(expired);
		Integer requestid = expired.getRequestId();
		StatusLink stat = new StatusLink(requestid, "expired");
		statuslinkService.update(stat);
		return "redirect:/search_contracts";
	}
	
	@PostMapping("/archive_contracts/{requestid}")
	public String archiveContract(@PathVariable("requestid") int requestid, Model model) {
		Contract foundContract = contractService.findContract(requestid).orElse(new Contract());
		foundContract.setArchived("T");
		contractService.addContract(foundContract);
		return "redirect:/search_contracts";
	}
	
	@GetMapping("/archive_contracts")
	public String getArchivedContracts(Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		model.addAttribute("contracts", contractService.getArchivedContracts());
		return "archive_contracts";
	}
	
	@PostMapping("/unarchive_contracts/{requestid}")
	public String unarchiveContract(@PathVariable("requestid") int requestid, Model model) {
		Contract unarchiveContract = contractService.findContract(requestid).orElse(new Contract());
		unarchiveContract.setArchived("F");
		contractService.addContract(unarchiveContract);
		return "redirect:/search_contracts";
	}
	
	@GetMapping("/unarchive_contracts")
	public String getUnarchivedContracts(Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		model.addAttribute("contracts", contractService.getAllContracts());
		return "search_contracts";
	}
	
	@GetMapping("/my_contracts")
    public String showMyContracts(Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails)principal).getUsername();
		User user = accountService.findUser(username);
		model.addAttribute("contracts", contractService.getContractsByUser(user.getUserid()));
		model.addAttribute("currentuser", user.getUsername());
        return "my_contracts";
    }
	
	//adds a contract to a users favourited contracts
	@PostMapping("/favourite_contracts/{requestid}")
	public String favouritedContract(@PathVariable("requestid") int requestid, Model model) {
		Contract favouritedContract = contractService.findContract(requestid).orElse(new Contract());
		Favourited favourite = new Favourited();
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails)principal).getUsername();
		User user = accountService.findUser(username);
		favourite.setUser(user);
		favourite.setContract(favouritedContract);
		favouritedService.addFavourite(favourite);
		return "redirect:/search_contracts";
	}

	@GetMapping("/favourite_contracts")
	public String getFavouritedContracts(Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails)principal).getUsername();
		User user = accountService.findUser(username);
		model.addAttribute("contracts", contractService.getFavouritedContracts(user.getUserid()));
		model.addAttribute("currentuser", user.getUsername());
		return "favourite_contracts";
	}
	
	//removes a contract from a users favourite contracts
	@PostMapping("/unfavourite_contracts/{requestid}")
	public String unfavouritContract(@PathVariable("requestid") int requestid, Model model) {
		Contract unfavouriteContract = contractService.findContract(requestid).orElse(new Contract());
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails)principal).getUsername();
		User user = accountService.findUser(username);
		contractService.unfavouriteContract(unfavouriteContract.getRequestid(), user.getUserid());
		contractService.addContract(unfavouriteContract);
		return "redirect:/search_contracts";
	}
	
	@GetMapping("/help")
	public String help() {
		return "/help";
	}
	
	@GetMapping("/reassign/{requestid}")
	public String reassignContractForm(@PathVariable("requestid") int requestid, Model model) {
		Integer i = currentService.getCurrent();
		currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
		repo.findById(requestid).ifPresent(contract->model.addAttribute("contract", contract));
		negRepo.findById(requestid).ifPresent(in_negotiation->model.addAttribute("in_negotiation", in_negotiation));
		List <User> users = contractService.getAllUsers();
		model.addAttribute("users", users);
		return "reassign";
	}
	
	@PostMapping("/api/reassignment")
	public String reassignContract(@Valid @ModelAttribute(name="contract") Contract contract, BindingResult br, Model model)
	{	
	Integer i = currentService.getCurrent();
	currentRepository.findById(i).ifPresent(current->model.addAttribute("currentCss", current));
	String fieldUpdatedList = "";
	String fieldBeforeList = "";
	String fieldAfterList = "";
	Contract foundContract = contractService.findContract(contract.getRequestid()).orElse(new Contract());
	Audit blank = new Audit();
	if (!foundContract.getUser().equals(contract.getUser())) {
		fieldBeforeList += ((String.valueOf((foundContract.getUser().getUserid()))))+ (", ");
		fieldAfterList += ((String.valueOf((contract.getUser().getUserid()))))+ (", ");
		fieldUpdatedList += ("userid") + (", ");
	}
		Date timeNow = new Date(Calendar.getInstance().getTimeInMillis());
		contract.setDate_updated(timeNow);
		blank.setField_after(fieldAfterList);
		blank.setField_before(fieldBeforeList);
		blank.setField_updated(fieldUpdatedList);
		blank.setUserid(contract.getUser());
		blank.setRequestedid(contract);
		blank.setDate(contract.getDate_updated());
		contractService.update(contract);
		auditService.addAudit(blank);
		return "redirect:/view_details/" + contract.getRequestid();
	}

}

  package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.enrities.Contact;
import com.smart.enrities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute 
	public void addCommonData(Model model,Principal principal) {
		
		String userName=principal.getName();
		System.out.println("USERNAME "+userName);
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("user "+user);
		
		model.addAttribute("user",user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
		}
	
	//processing add contact
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal,HttpSession session) {
		
		 
		try {
		String name=principal.getName();
		
	     User user=this.userRepository.getUserByUserName(name);
	     
	     //processing and uploading file
	     
	     if (file.isEmpty()) {
			System.out.println("File is empty");
			
			contact.setImage("contact.png");
	    	 
		}else {
			
			contact.setImage(file.getOriginalFilename());
			
			File saveFile=new ClassPathResource("static/img").getFile();
			 
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING); 
			System.out.println("image is uploaded");
		}
	     
	     
	     contact.setUser(user);
	     
		
	     user.getContact().add(contact);
	     this.userRepository.save(user);
	     
		System.out.println("DATA "+contact);
		
		System.out.println("added to database");
		
		//message success
	 session.setAttribute("message", 
			 new Message("Your contact is added !! Add more.....","success"));
		
		}catch (Exception e) {

      System.out.println("ERROR"+e.getMessage());
      e.printStackTrace();
      
      //message error
      session.setAttribute("message", new Message("Some went wrong !! Try agin....","danger"));
      
		}
		 return "normal/add_contact_form";
	}
	
	
	//show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal) {
		
		model.addAttribute("title","Show User Contact!!");
		
		String userName=principal.getName();
		
		User user=this.userRepository.getUserByUserName(userName);
		
		Pageable pageable =PageRequest.of(page, 2);
		
	   Page<Contact> contacts= this.contactRepository.findContactsByUser(user.getId(),pageable);
		
	   model.addAttribute("contacts",contacts);
	   model.addAttribute("currentPage",page);
	   model.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	//showing particular contact detail
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(Model model,
	@PathVariable("cId") Integer cId,Principal principal) {
		
		System.out.println("CID "+cId);
		Optional<Contact> contact=this.contactRepository.findById(cId);
		
		Contact contactOptional=contact.get();
		
		String userName=principal.getName();
		
		User user=this.userRepository.getUserByUserName(userName);
		
		
		if(user.getId()==contactOptional.getUser().getId()) {
			
			model.addAttribute("contact",contactOptional);
		}
		
		
		return "normal/contact_detail";
		
		
	}
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,
			Principal principal,HttpSession session) {
		
		Contact contact=this.contactRepository.findById(cId).get();
		
		
       
		
		User user=this.userRepository.getUserByUserName(principal.getName());
        
		user.getContact().remove(contact);
		this.userRepository.save(user);
        	 
        session.setAttribute("message", new Message("Contact Deleted successfully...","success"));
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	//update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cid,Model model) {
		
		model.addAttribute("title","Update Contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact",contact);
		
		return "normal/update-form";
		
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact
			,@RequestParam("profileImage") MultipartFile file,Model model
			,HttpSession session,Principal principal) 
	{
		
		try {
			
			// old contact detail
			Contact oldContactDetail=this.contactRepository.findById(contact.getcId()).get(); 
			 
			
		//image....
			
			
			
			if (!file.isEmpty()) {
				
				//file work...
				//rewrite
				//delete old photo
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				
				
				contact.setImage(oldContactDetail.getImage());
				//update new photo
				
				File saveFile=new ClassPathResource("static/img").getFile();
				 
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING); 
				
				contact.setImage(file.getOriginalFilename());
				
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated","alert-success"));
			System.out.println("CID" +contact.getcId());
		System.out.println("contact name "+contact.getName());
		}catch (Exception e) {
           
			e.printStackTrace();

		} 
		return "redirect:/user/"+contact.getcId()+"/contact";
		
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		
		model.addAttribute("title","Profile Page");
		return "normal/profile";
		}
	
	//open setting handler
	@GetMapping("/settings") 
	public String openSetting() {
		
		return "normal/settings";
		
}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,Principal principal,
			HttpSession session) {
		  
	 String userName=principal.getName();
		User currentUser=this.userRepository.getUserByUserName(userName);
		
		System.out.println(currentUser.getPassword());
		
		System.out.println("OLD password :-"+oldPassword);
		System.out.println("NEW password :-"+newPassword);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())) {
			//change password
			
		currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(currentUser);
		session.setAttribute("message",new Message("Your password is successfully changed", "alert-success"));
		}else {
			//error
			
			session.setAttribute("message",new Message("Please Enter correct Password", "elert-error"));
		}
		
		 
		return "redirect:/user/settings";
			
		
	
	}
	
	
	
}

package main.domain;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = operative.TABLE_NAME)
//@SecondaryTable (name = Contract.TABLE_NAME)
public class operative {
	
	@Id
	@Column(name = "requestid")
	private Integer requestid;
	
	@NotNull
	@Column(name = "date_signed")
	private Date date_signed;
	
	@NotNull
	@Column(name = "date_commenced")
	private Date date_commenced;
	
	@NotNull
	@Column(name = "date_expire")
	private Date date_expire;
	
	@NotNull
	@Column(name = "verification")
	private String verification;
	
	@OneToOne
    @MapsId
	private Contract contract;
	
	//Constructor method
	public operative(Integer requestid, Date date_signed, Date date_commenced, Date date_expire, String verification, Contract contract) {
		super();
		this.requestid = requestid;
		this.date_signed = date_signed;
		this.date_commenced = date_commenced;
		this.date_expire = date_expire;
		this.verification = verification;
		this.contract = contract;
	}

	//Getters and setters
	public Integer getRequestId() {
		return requestid;
	}

	public void setRequestId(Integer requestid) {
		this.requestid = requestid;
	}
	
	public Contract getContract() {
		return contract;
	}
	
	public void setContract() {
		this.contract = contract;
	}
	
	public Date getDate_signed() {
		return date_signed;
	}

	public void setDate_signed(Date date_signed) {
		this.date_signed = date_signed;
	}

	public Date getDate_commenced() {
		return date_commenced;
	}

	public void setDate_commenced(Date date_commenced) {
		this.date_commenced = date_commenced;
	}

	public Date getDate_expire() {
		return date_expire;
	}

	public void setDate_expire(Date date_expire) {
		this.date_expire = date_expire;
	}

	public String getVerification() {
		return verification;
	}

	public void setVerification(String verification) {
		this.verification = verification;
	}

	@Override
	public String toString() {
		return "operative [date_signed=" + date_signed + ", date_commenced=" + date_commenced + ", date_expire="
				+ date_expire + ", verification=" + verification + "]";
	}
	
	
}

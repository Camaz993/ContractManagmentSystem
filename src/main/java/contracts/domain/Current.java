/** 
** The current domain class includes all of the columns that match the database along with the getter and setter methods, and the to string method
 * @author Alice, Caleb, Laurie, Natalie, Poppy
 */
package contracts.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = Current.TABLE_NAME)
public class Current {
	
	public static final String TABLE_NAME = "current_css";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idcurrent_css")
	private Integer idcurrent_css;
	
	@Column(name = "colour")
	private String colour;
	
	public Current(Integer idcurrent_css, String colour) {
		super();
		this.idcurrent_css = idcurrent_css;
		this.colour=colour;
	}
	
	public Current() {
		
	}
	
	public Integer getIdcurrent_css() {
		return idcurrent_css;
	}
	
	public void setIdcurrent_css(Integer idcurrent_css) {
		this.idcurrent_css = idcurrent_css;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	@Override
	public String toString() {
		return "Current [idcurrent_css=" + idcurrent_css + ", colour=" + colour + "]";
	}

	
	
}

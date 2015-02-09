package canive;

import java.util.Vector;

public class Ciudad {
	private String titulo;
	private String descripcion;
	private Vector tmax;
	private Vector tmin;
	private Vector tmaxF;
	private Vector tminF;
	private Vector estado;
	private String postal;
	private String tactual;
	
	
//	public String getPostal() {
//		if (this.postal==null) {
//			postal = "";
//		}
//		return postal;
//	}
//	public void setPostal(String postal) {
//		this.postal = postal;
//	}
	public Vector getTmax() {
		return tmax;
	}
	public void setTmax(Vector tmax) {
		this.tmax = tmax;
		this.cambiarMetricaMax();
	}
	public Vector getTmin() {
		return tmin;
	}
	public void setTmin(Vector tmin) {
		this.tmin = tmin;
		this.cambiarMetricaMin();
	}
	public Vector getTmaxF() {
		return tmaxF;
	}
	public Vector getTminF() {
		return tminF;
	}
	public Vector getEstado() {
		return estado;
	}
	public void setEstado(Vector estado) {
		this.estado = estado;
	}
	
	public String getTactual() {
		return tactual;
	}
	public void setTactual(String tactual) {
		this.tactual = tactual;
	}
	public Ciudad(String titulo, String descripcion, Vector tmax,
			Vector tmin,Vector tmaxF, Vector tminF, Vector estado, String postal, String tactual) {
		super();
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.tmax = tmax;
		this.tmin = tmin;
		this.tmax = tmaxF;
		this.tmin = tminF;
		this.estado = estado;
		this.tactual = tactual;
		if (this.postal.length()!=0) {
			this.postal = postal;
		}
		else
		{
			this.postal = "Indef";
		}
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Ciudad() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void cambiarMetricaMax()
    {
    	this.tmaxF = new Vector();
    	int i = 0;
    	while (i<5) {
    		int CelciusMax = Integer.parseInt(this.tmax.elementAt(i).toString());
			int FarMax = (int) ((CelciusMax/0.55)+32);
			this.tmaxF.addElement(Integer.toString(FarMax));
			i++;
		}	
    }
	
	public void cambiarMetricaMin()
    {
    	this.tminF = new Vector();
    	int i = 0;
    	while (i<5) {			
			int CelciusMin = Integer.parseInt(this.tmin.elementAt(i).toString());
			int FarMin = (int) ((CelciusMin/0.55)+32);
			this.tminF.addElement(Integer.toString(FarMin));
			i++;
		}		
    }
	
}

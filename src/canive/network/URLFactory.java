package canive.network;

public class URLFactory {
	private String url="";	
	private String protocol="";
	private String host="";
	private String port="";
	private String absPath="";
	private boolean isSecured=false;
	
	private int portBeginsAt=-1;
	private int absolutePathBeginsAt=-1;
	private int httpParamIndex=-1;
	
	
	public URLFactory(String url){		
		this.url = url.trim();
		
		if(this.url.indexOf("://")!=-1){
			protocol = this.url.substring(0, this.url.indexOf("://")+3);
			this.url = this.url.substring(this.url.indexOf("://")+3, this.url.length());
		} else{
			protocol = "http://";
		}
		protocol = protocol.toLowerCase();
		
		if(protocol.indexOf("https")!=-1 || protocol.indexOf("ssl")!=-1 || protocol.indexOf("tls")!=-1 ){
			isSecured = true;
		}
		
		httpParamIndex = this.url.indexOf('?');		
		
		if(httpParamIndex!=-1 && this.url.indexOf(':')!=-1 && this.url.indexOf(':')<httpParamIndex){
			portBeginsAt = this.url.indexOf(':');
		} else if(httpParamIndex==-1 && this.url.indexOf(':')!=-1){
			portBeginsAt = this.url.indexOf(':');
		}
		
		if(httpParamIndex!=-1 && this.url.indexOf('/')!=-1 && this.url.indexOf('/')<httpParamIndex){
			absolutePathBeginsAt = this.url.indexOf('/');
		} else if(httpParamIndex==-1 && this.url.indexOf('/')!=-1){
			absolutePathBeginsAt = this.url.indexOf('/');
		} 
		
		if(portBeginsAt!=-1)
			host = this.url.substring(0, portBeginsAt);
		else if(absolutePathBeginsAt!=-1)
			host = this.url.substring(0, absolutePathBeginsAt);
		else 
			host = this.url.substring(0, this.url.length());
		
		if(portBeginsAt!=-1){
			if(absolutePathBeginsAt!=-1){
				port = this.url.substring(portBeginsAt+1, absolutePathBeginsAt);
			} else{
				port = this.url.substring(portBeginsAt+1, this.url.length());
			}
		} else{
			if(isSecured)
				port = "443";
			else
				port = "80";
		}
		
		if(absolutePathBeginsAt!=-1){
			absPath = this.url.substring(absolutePathBeginsAt, this.url.length());
		} else{
			absPath = "/";
		}
		
		
	}
	
	
	public String getHost() {
		return host;
	}

	public boolean isSecured() {
		return isSecured;
	}

	public String getHttpTcpCellularUrl(String apn, String apnUser, String apnPassword){
		if(apn!=null && apn.length()>0)
			apn = ";apn="+apn;		
		else
			apn = "";
		
		if(apnUser!=null && apnUser.length()>0)
			apnUser = ";TunnelAuthUsername="+apnUser;
		else
			apnUser = "";
		
		if(apnPassword!=null && apnPassword.length()>0)
			apnPassword = ";TunnelAuthPassword="+apnPassword;
		else
			apnPassword = "";
		
		if(isSecured()){
			return "https://" + host + ":" + port + absPath + ";deviceside=true" + apn+apnUser+apnPassword;			
		} else{
			return "http://" + host + ":" + port + absPath + ";deviceside=true" + apn+apnUser+apnPassword;
		}
	}
	
	
	public String getHttpTcpWiFiUrl(){
		if(isSecured()){
			return "https://" + host + ":" + port + absPath + ";interface=wifi";			
		} else{
			return "http://" + host + ":" + port + absPath + ";interface=wifi";
		}
	}
	
	
}

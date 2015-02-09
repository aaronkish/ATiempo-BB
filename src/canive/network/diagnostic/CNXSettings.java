package canive.network.diagnostic;

public class CNXSettings {

	private int tries;
	private boolean useGET;
	private int methodPostSize;
	private boolean useTCP;
	private String tcpAPN;
	private String tcpUser;
	private String tcpPass;
	private String url1;
	private String url2;
	
	
	
	public CNXSettings(int tries, boolean useGET, int methodPostSize, boolean useTCP, String tcpAPN, String tcpUser,
			String tcpPass, String url1, String url2) {
		super();
		this.tries = tries;
		this.useGET = useGET;
		this.methodPostSize = methodPostSize;
		this.useTCP = useTCP;
		this.tcpAPN = tcpAPN;
		this.tcpUser = tcpUser;
		this.tcpPass = tcpPass;
		this.url1 = url1;
		this.url2 = url2;
	}

	public CNXSettings() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getTries() {
		return tries;
	}

	public void setTries(int tries) {
		this.tries = tries;
	}

	public boolean isUseGET() {
		return useGET;
	}

	public void setUseGET(boolean useGET) {
		this.useGET = useGET;
	}

	public int getMethodPostSize() {
		return methodPostSize;
	}

	public void setMethodPostSize(int methodPostSize) {
		this.methodPostSize = methodPostSize;
	}

	public boolean isUseHTTP() {
		return true; //CONSTANTE PORQUE NO SE USA SOCKET
	}

	public boolean isUseTCP() {
		return useTCP;
	}

	public void setUseTCP(boolean useTCP) {
		this.useTCP = useTCP;
	}

	public String getTcpAPN() {
		return tcpAPN;
	}

	public void setTcpAPN(String tcpAPN) {
		this.tcpAPN = tcpAPN;
	}

	public String getTcpUser() {
		return tcpUser;
	}

	public void setTcpUser(String tcpUser) {
		this.tcpUser = tcpUser;
	}

	public String getTcpPass() {
		return tcpPass;
	}

	public void setTcpPass(String tcpPass) {
		this.tcpPass = tcpPass;
	}

	public String getUrl1() {
		return url1;
	}

	public void setUrl1(String url1) {
		this.url1 = url1;
	}

	public String getUrl2() {
		return url2;
	}

	public void setUrl2(String url2) {
		this.url2 = url2;
	}
	
}

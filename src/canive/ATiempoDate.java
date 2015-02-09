package canive;


public class ATiempoDate {
	//private String today;
	private String currentDay;
	private String currentMonth;
	private String currentYear;
	private String []meses = new String[] {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
	private int []diasMeses = {31,28,31,30,31,30,31,31,30,31,30,31};
	private int []diasMesesBis = {31,29,31,30,31,30,31,31,30,31,30,31};
	//private String []dias = new String[] {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
	//private String []diasAbrev = new String[] {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
	
	public ATiempoDate(String today, String currentDay, String currentMonth,
			String currentYear) {
		super();
		//this.today = today;
		this.currentDay = currentDay;
		this.currentMonth = currentMonth;
		this.currentYear = currentYear;
	}

	public ATiempoDate() {
		super();
	}
	
//	public String getCurrentDay() {
//		return currentDay;
//	}

	public void setCurrentDay(String currentDay) {
		this.currentDay = currentDay;
	}

//	public String getCurrentMonth() {
//		return currentMonth;
//	}

	public void setCurrentMonth(String currentMonth) {
		this.currentMonth = currentMonth;
	}

//	public String getCurrentYear() {
//		return currentYear;
//	}	

	public void setCurrentYear(String currentYear) {
		this.currentYear = currentYear;
	}	

//	public String getToday() {
//		return today;
//	}
//
//	public void setToday(String today) {
//		this.today = today;
//	}
	
	public String getNextDateString(int offset)
	{
		String constructedDate = "";
		try {
			if (offset>0 && offset<6) {
				constructedDate = calculateMonth(offset)+" "+calculateDay(offset)+", "+calculateYear(offset);
			}
		} catch (Exception e) {
			//MANEJAR EXCEPCION
			System.out.println(e.toString());
		}
		return constructedDate;
	}
	
	private String calculateMonth(int offset)
	{
		int pos = 0;
		//Busca la posicion del mes para buscar la cantidad de dias que tiene
		for (int i = 0; i < meses.length; i++) {
			if (this.currentMonth.equals(meses[i])) {
				pos = i;
				break;
			}
		}
		int cantDias = 0;
		//Si es Febrero verificar que sea bisiesto
		if (this.currentMonth.equals(meses[1])) {
			if (Integer.parseInt(this.currentYear)%4==0) {
				cantDias = this.diasMesesBis[pos];
			}
			else
			{
				cantDias = this.diasMeses[pos];
			}
		}
		else
		{
			cantDias = this.diasMeses[pos];
		}
		
		if ((Integer.parseInt(this.currentDay)+offset)>cantDias) {
			if (pos<11) {
				return meses[pos+1];
			}
			else
			{
				return meses[0];
			}
		}	
		else
		{
			return this.currentMonth;
		}
	}
	
	private String calculateDay(int offset)
	{
		int pos = 0;
		//Busca la posicion del mes para buscar la cantidad de dias que tiene
		for (int i = 0; i < meses.length; i++) {
			if (this.currentMonth.equals(meses[i])) {
				pos = i;
				break;
			}
		}
		int cantDias = 0;
		//Si es Febrero verificar que sea bisiesto
		if (this.currentMonth.equals(meses[1])) {
			if (Integer.parseInt(this.currentYear)%4==0) {
				cantDias = this.diasMesesBis[pos];
			}
			else
			{
				cantDias = this.diasMeses[pos];
			}
		}
		else
		{
			cantDias = this.diasMeses[pos];
		}
		
		if ((Integer.parseInt(this.currentDay)+offset)>cantDias) {
			return String.valueOf((Integer.parseInt(this.currentDay)+offset)-cantDias);
		}	
		else
		{
			return String.valueOf(Integer.parseInt(this.currentDay)+offset);
		}
	}
	
	private String calculateYear(int offset)
	{
		if (!this.currentMonth.equals(meses[11])) {
			return this.currentYear;
		}
		else if ((Integer.parseInt(this.currentDay)+offset)>31) 
		{
			int newyear = Integer.parseInt(this.currentYear)+1;
			return String.valueOf(newyear);
		}
		else
		{
			return this.currentYear;
		}
		
	}
}

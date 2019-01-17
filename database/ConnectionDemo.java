package database;

import java.util.ArrayList;

public class ConnectionDemo {
	private String pwd;
	private String user;
	
	public ConnectionDemo(){
		pwd = DBDetails.password;
		user = DBDetails.username;
		testConnection();
	}
	
	/**
	 * Here you can see example usage of the MyConnection class. Be sure to fullyResetTables at least once after downloading and 
	 * running this package. This ensures tables are rebuilt fresh and ready to use!
	 */
	public void testConnection(){
		MyConnection c = new MyConnection(user, pwd);

		boolean fullyResetTables = true; //Set this to true if you wish to rebuild/reset your tables!
		PrepStudentScript initTables = new PrepStudentScript(fullyResetTables); //called to initialize our tables
		
		//clear all DBStudent information so we have a fresh db
		c.deleteAllSavedStudent();
		
		//clear all course info and repopulate it with the provided course list
		c.repopulateCourses();

		//test addCourse
		c.addCourse("CIS*9999", "0.75", "Oopsies", "never", "");

		//test deleteCourse
		c.deleteCourse("CIS*9999");

		//test getAllCourses
		ArrayList<String> courses = c.getAllCourses();
		for (String course : courses){
			System.out.println(course);
		}
		
		//test getCourse for CIS*1500
		String course = c.findCourse("CIS*1500");
		System.out.println(course);

		//test save student
		ArrayList<String> cl = new ArrayList<String>();
		cl.add("CIS*3530,0.5,Data Base Systems and Concepts,F19,CIS*2520"); // A future course i plan to take
		cl.add("CIS*2520,0.5,Data Structures,W18,CIS*2500");                // A future course i plan to take
		cl.add("CIS*1500,F16,87,Completed");                                // What an attempt might look like (you format these however you need!)
		DBStudent s = new DBStudent("123123","Matt","MSC",cl);

		c.saveStudent(s);

		//test load student
		DBStudent loadedS = c.loadStudent("123123","Matt");

		System.out.println(loadedS.getId());
		System.out.println(loadedS.getName());
		System.out.println(loadedS.getDegree());
		cl = loadedS.getCourses();
		System.out.println(loadedS.getCourses());
		for (String cInfo : cl){
			//lets get each course from our course list!
			System.out.println(c.findCourse(cInfo.split(",")[0]));
		}

		//test clear Courses
		c.deleteAllCourses();

		//test delete specific student
		c.deleteSavedStudent("123123","Matt");
		loadedS = c.loadStudent("123123", "Matt");
		System.out.println(loadedS.getId());

		courses = c.getAllCourses();
		for (String course2 : courses){
			System.out.println(course);
		}

	}
	
	public static void main (String[] args){
		ConnectionDemo p = new ConnectionDemo();
	}

}

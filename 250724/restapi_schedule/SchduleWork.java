package work;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SchduleWork {

	public static void main(String[] args) throws IOException {
		URL address=new URL("https://jsonplaceholder.typicode.com/users");

		InputStream in=address.openStream();
		int read=0;
		String buf="";
		while((read=in.read())!=-1) {
			buf+=(char)read;
		}
		//buf = buf.replaceAll("\\s*", "");
		System.out.println(buf);
		
		
		ObjectMapper mapper = new ObjectMapper();
		//id가 접근오류가 발생하는 이유는 User객체의 변수들이 private일 때 발생하므로 Public로 변경해야한다.
		//접근을 set함수가 아닌 .속성으로 접근하도록 허용해야 한다.
		List<User> users = mapper.readValue(buf, new TypeReference<List<User>>() {});
		//User[] users = mapper.readValue(buf, User[].class);

		System.out.println(users);
		
		//데이터베이스 생성하기(아래 코드가 있음)
		//데이터베이스 접속하는 객체를 만들어 입력
		insertUsers(users);

	}
	
	private static void insertUsers(List<User> list) throws ClassNotFoundException, SQLException {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		try(Connection conn=DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:xe"
				,"test","1111")){
			conn.setAutoCommit(false);
			String geoSql="insert into geo(id,lat,lng) values(?,?,?)";
			String addressSql="insert into address(id,street, suite, city,zipcode,geo_id) values(?,?,?,?,?,?)";
			String companySql="insert into company(id,name,catchPhrase,bs) values(?,?,?,?)";
			String userSql="insert into users(id,name,username,email,phone,website,address_id,company_id) values(?,?,?,?,?,?,?,?)";
			
			//user의 id를 제외하고는 address, company, geo에 id는 자동시퀸스생성
			//입력순서는 역으로 처리해야한다.(geo-address-company-user)
			try(
			PreparedStatement geops=conn.prepareStatement(geoSql);
			PreparedStatement addressps=conn.prepareStatement(addressSql);
			PreparedStatement companyps=conn.prepareStatement(companySql);
			PreparedStatement userps=conn.prepareStatement(userSql);
			){
				String geoId="select geo_seq.nextval from dual";
				String addressId="select address_seq.nextval from dual";
				String companyId="select company_seq.nextval from dual";
				Statement seqStat=conn.createStatement();
				
				for(User user:list) {
					//Geo에 id생성
					ResultSet rsGeo=seqStat.executeQuery(geoId);
					rsGeo.next();
					int geoid=rsGeo.getInt(1); //geo의 id추출
					
					//Geo테이블삽입
					geops.setInt(1,geoid);
					geops.setString(2, user.address.geo.lat);
					geops.setString(3, user.address.geo.lng);
					geops.executeUpdate();
					
					//address id생성/테이블입력
					ResultSet rsAddress=seqStat.executeQuery(addressId);
					rsAddress.next();
					int addressid=rsAddress.getInt(1);
					
					//insert into address
					//(id,street, suite, city,zipcode,geo_id) values(?,?,?,?,?,?)
					addressps.setInt(1, addressid);
					addressps.setString(2,user.getAddress().getStreet());
					addressps.setString(3,user.getAddress().getSuite());
					addressps.setString(4,user.getAddress().getCity());
					addressps.setString(5,user.getAddress().getZipcode());
					addressps.setInt(6,geoid);
					addressps.executeUpdate();
					
					//company id생성/테이블입력
					ResultSet rsCompany=seqStat.executeQuery(companyId);
					rsCompany.next();
					int companyid=rsCompany.getInt(1);
					//insert into company(id,name,catchPhrase,bs) values(?,?,?,?)
					companyps.setInt(1, companyid);
					companyps.setString(2,user.getCompany().getName());
					companyps.setString(3,user.getCompany().getCatchPhrase());
					companyps.setString(4,user.getCompany().getBs());
					companyps.executeUpdate();
					
					//user 테이블입력
					//insert into users
					//(id,name,username,email,phone,website,address_id,company_id) values(?,?,?,?,?,?,?,?)";
					userps.setInt(1,user.getId());
					userps.setString(2,user.getName());
					userps.setString(3,user.getUsername());
					userps.setString(4,user.getEmail());
					userps.setString(5,user.getPhone());
					userps.setString(6,user.getWebsite());
					userps.setInt(7,addressid);
					userps.setInt(8,companyid);
					userps.executeUpdate();
				}
				//반복문으로 입력 후 commit처리
				conn.commit();
				System.out.println("모든 데이터 정상입력");
				//모든 자원에 대한 반환
			}catch (Exception e) {
				conn.rollback();
				e.printStackTrace();
			}
			
		}
		
	}

}


/*
USERS테이블이 모든 객체를 취합하는 테이블이고, 하부에 ADDRESS, GEO, COMPANY가 존재함
하지만 USERS객체에는 하위 3개 테이블의 정보가 존재하지 않음
하위의 3개 테이블안에 USER테이블의 외래키가 존재하고 객체가 아닌 USER_ID를 가지고 있음
하지만 자바 객체(USERS)는 json의 구조와 일치되게 구성되어 있으며 id가 아닌 객체로서 참조를 하고 있음
결론:json==java객체, 테이블은 반대로 구성되어 있음


CREATE TABLE USERS (
  ID NUMBER PRIMARY KEY,
  NAME VARCHAR2(100),
  USERNAME VARCHAR2(100),
  EMAIL VARCHAR2(100),
  PHONE VARCHAR2(50),
  WEBSITE VARCHAR2(100)
);

CREATE TABLE ADDRESS (
  ID NUMBER PRIMARY KEY,
  USER_ID NUMBER REFERENCES USERS(ID),
  STREET VARCHAR2(100),
  SUITE VARCHAR2(100),
  CITY VARCHAR2(100),
  ZIPCODE VARCHAR2(20)
);

CREATE TABLE GEO (
  ID NUMBER PRIMARY KEY,
  ADDRESS_ID NUMBER REFERENCES ADDRESS(ID),
  LAT VARCHAR2(20),
  LNG VARCHAR2(20)
);

CREATE TABLE COMPANY (
  ID NUMBER PRIMARY KEY,
  USER_ID NUMBER REFERENCES USERS(ID),
  NAME VARCHAR2(100),
  CATCH_PHRASE VARCHAR2(255),
  BS VARCHAR2(255)
);

*/
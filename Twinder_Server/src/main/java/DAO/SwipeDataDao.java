package DAO;
import Model.Swipe;
import java.sql.*;
import java.util.ArrayList;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;


public class SwipeDataDao {
  private static BasicDataSource dataSource;


  public SwipeDataDao() {
    dataSource = DBCPDataSource.getDataSource();
  }
  public void createSwipeData(Swipe newSwipe) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "INSERT INTO SwipeData (swiper, swipee, comment, leftorright) " +
        "VALUES (?,?,?,?)";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setInt(1, newSwipe.getSwiper());
      preparedStatement.setInt(2, newSwipe.getSwipee());
      preparedStatement.setString(3, newSwipe.getComment());
      preparedStatement.setString(4, newSwipe.getLeftorright());

      // execute insert SQL statement
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }
  public ArrayList<Integer> getMatches(Integer swiper) {
    ArrayList<Integer> swipeeList= new ArrayList<Integer>();
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "Select swipee from SwipeData "
        + "where swiper = ? and leftorright = \"right\""
        + "limit 100;";
    ResultSet results = null;
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setInt(1, swiper);

      // execute insert SQL statement
      results = preparedStatement.executeQuery();
      while(results.next()){
        int swipeeId = results.getInt("swipee");
        swipeeList.add(swipeeId);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return swipeeList;
  }

  public ArrayList<Integer> getStats(Integer swiper) {
    ArrayList<Integer> resList= new ArrayList<Integer>();
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "Select COUNT(Distinct IF(leftorright = \"left\", swipee, NULL)) AS numdislike, COUNT(Distinct IF(leftorright = \"right\", swipee, NULL)) AS numlike From SwipeData where swiper =?";
    ResultSet results = null;
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setInt(1, swiper);

      // execute insert SQL statement
      results = preparedStatement.executeQuery();
      if(results.next()){
        int numdislike = results.getInt("numdislike");
        int numlike = results.getInt("numlike");
        resList.add(numlike);
        resList.add(numdislike);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return resList;
  }



}

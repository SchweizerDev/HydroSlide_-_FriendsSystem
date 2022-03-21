package ch.luca.hydroslide.friends.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import ch.luca.hydroslide.friends.Friends;
import lombok.Getter;

public class Database {

	@Getter
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	public void executeQuery(String statement, boolean async, Consumer<ResultSet> consumer) {
		if(!async) {
			Connection connection = null;
			try {
				connection = Friends.getInstance().getSqlPool().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				ResultSet resultSet = preparedStatement.executeQuery();
				consumer.accept(resultSet);
				preparedStatement.close();
				resultSet.close();
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				if(connection != null) {
					try {
						connection.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return;
		}
		executor.execute(() -> {
			Connection connection = null;
			try {
				connection = Friends.getInstance().getSqlPool().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				ResultSet resultSet = preparedStatement.executeQuery();
				consumer.accept(resultSet);
				preparedStatement.close();
				resultSet.close();
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				if(connection != null) {
					try {
						connection.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	public void update(String statement, boolean async) {
		if(!async) {
			Connection connection = null;
			try {
				connection = Friends.getInstance().getSqlPool().getConnection();
				
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if(connection != null) {
						connection.close();
					}
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			return;
		}
		executor.execute(() -> {
			Connection connection = null;
			try {
				connection = Friends.getInstance().getSqlPool().getConnection();
				
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if(connection != null) {
						connection.close();
					}
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		});
	}
}

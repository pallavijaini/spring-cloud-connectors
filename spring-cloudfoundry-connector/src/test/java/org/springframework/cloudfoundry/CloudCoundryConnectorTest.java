package org.springframework.cloudfoundry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.cloudfoundry.CloudFoundryConnectorTestHelper.getApplicationInstanceInfo;
import static org.springframework.cloudfoundry.CloudFoundryConnectorTestHelper.getMysqlServicePayload;
import static org.springframework.cloudfoundry.CloudFoundryConnectorTestHelper.getPostgresqlServicePayload;
import static org.springframework.cloudfoundry.CloudFoundryConnectorTestHelper.getRedisServicePayload;
import static org.springframework.cloudfoundry.CloudFoundryConnectorTestHelper.getMongoServicePayload;
import static org.springframework.cloudfoundry.CloudFoundryConnectorTestHelper.getRabbitServicePayload;
import static org.springframework.cloudfoundry.CloudFoundryConnectorTestHelper.getServicesPayload;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloudfoundry.CloudFoundryConnector.EnvironmentAccessor;

/**
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudCoundryConnectorTest {
	private CloudFoundryConnector testCloudConnector = new CloudFoundryConnector();
	@Mock EnvironmentAccessor mockEnvironment;

	private static final String hostname = "10.20.30.40";
	private static final int port = 1234;
	private static String username = "myuser";
	private static final String password = "mypass";

	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testCloudConnector.setCloudEnvironment(mockEnvironment);
	}
	
	@Test
	public void isInMatchingEnvironment() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("myapp", "http://myapp.com"));
		assertTrue(testCloudConnector.isInMatchingCloud());
		
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(null);
		assertFalse(testCloudConnector.isInMatchingCloud());
	}
	
	@Test
	public void mysqlServiceCreation() {
		String[] versions = {"5.1", "5.5"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
								getMysqlServicePayload(version, "mysql-1", hostname, port, username, password, "database-123"),
								getMysqlServicePayload(version, "mysql-2", hostname, port, username, password, "database-123")));
		}
		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();
		assertNotNull(getServiceInfo(serviceInfos, "mysql-1"));
		assertNotNull(getServiceInfo(serviceInfos, "mysql-2"));
	}

	@Test
	public void postgresqlServiceCreation() {
		String[] versions = {"9.1", "9.2"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
								getPostgresqlServicePayload(version, "postgresql-1", hostname, port, username, password, "database-123"),
								getPostgresqlServicePayload(version, "postgresql-2", hostname, port, username, password, "database-123")));
		}

		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();
		assertNotNull(getServiceInfo(serviceInfos, "postgresql-1"));
		assertNotNull(getServiceInfo(serviceInfos, "postgresql-2"));
	}

	@Test
	public void redisServiceCreation() {
		String[] versions = {"2.0", "2.2"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getRedisServicePayload(version, "redis-1", hostname, port, password, "redis-db"),
						getRedisServicePayload(version, "redis-2", hostname, port, password, "redis-db")));
		}

		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();
		assertNotNull(getServiceInfo(serviceInfos, "redis-1"));
		assertNotNull(getServiceInfo(serviceInfos, "redis-2"));
	}
	
	@Test
	public void mongoServiceCreation() {
		String[] versions = {"2.0", "2.2"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getMongoServicePayload(version, "mongo-1", hostname, port, username, password, "inventory-1", "db"),
						getMongoServicePayload(version, "mongo-2", hostname, port, username, password, "inventory-2", "db")));
		}

		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();
		assertNotNull(getServiceInfo(serviceInfos, "mongo-1"));
		assertNotNull(getServiceInfo(serviceInfos, "mongo-2"));
	}

	@Test
	public void rabbitServiceCreation() {
		String[] versions = {"2.0", "2.2"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getRabbitServicePayload(version, "rabbit-1", hostname, port, username, password, "q-1", "vhost1"),
						getRabbitServicePayload(version, "rabbit-2", hostname, port, username, password, "q-2", "vhost2")));
		}

		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();
		assertNotNull(getServiceInfo(serviceInfos, "rabbit-1"));
		assertNotNull(getServiceInfo(serviceInfos, "rabbit-2"));
	}
	
	@Test
	public void applicationInstanceInfo() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("my-app", "foo.cf.com", "bar.cf.com"));
		
		assertEquals("my-app", testCloudConnector.getApplicationInstanceInfo().getAppId());
		assertEquals(Arrays.asList("foo.cf.com", "bar.cf.com"), testCloudConnector.getApplicationInstanceInfo().getProperties().get("uris"));
	}
	
	
	private static ServiceInfo getServiceInfo(List<ServiceInfo> serviceInfos, String serviceId) {
		for (ServiceInfo serviceInfo : serviceInfos) {
			if (serviceInfo.getId().equals(serviceId)) {
				return serviceInfo;
			}
		}
		return null;
	}
	

}

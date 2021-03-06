/**
 * Copyright 2013-2014 Sylvain Cadilhac (NetFishers)
 */
package onl.netfishers.netshot.work.tasks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;

import onl.netfishers.netshot.Database;
import onl.netfishers.netshot.TaskManager;
import onl.netfishers.netshot.device.Domain;
import onl.netfishers.netshot.device.Network4Address;
import onl.netfishers.netshot.device.credentials.DeviceCredentialSet;
import onl.netfishers.netshot.work.Task;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This task scans a subnet to discover devices.
 */
@Entity
public class ScanSubnetsTask extends Task {
	
	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ScanSubnetsTask.class);
	
	/** The subnets. */
	private Set<Network4Address> subnets;
	
	/** The domain. */
	private Domain domain;
	
	/**
	 * Instantiates a new scan subnet task.
	 */
	protected ScanSubnetsTask() {
	}
	
	/**
	 * Instantiates a new scan subnets task.
	 *
	 * @param subnets the subnets
	 * @param domain the domain
	 * @param comments the comments
	 */
	public ScanSubnetsTask(Set<Network4Address> subnets, Domain domain, String comments,
			String target, String author) {
		super(comments, target, author);
		this.domain = domain;
		this.subnets = subnets;
	}

	/* (non-Javadoc)
	 * @see onl.netfishers.netshot.work.Task#getTaskDescription()
	 */
	@Override
	@XmlElement
	@Transient
	public String getTaskDescription() {
		return "Subnet scan";
	}

	/* (non-Javadoc)
	 * @see onl.netfishers.netshot.work.Task#run()
	 */
	@SuppressWarnings("unchecked")
  @Override
	public void run() {
		logger.debug("Starting scan subnet process.");
		
		Session session = Database.getSession();
		Set<Integer> toScan = new HashSet<Integer>();
		List<DeviceCredentialSet> knownCommunities;
		try {
			try {
				for (Network4Address subnet : subnets) {
					int address1 = subnet.getSubnetMin();
					int address2 = subnet.getSubnetMax();
					int min = (address1 > address2 ? address2 : address1);
					int max = (address1 > address2 ? address1 : address2);
					if (min < max - 1) {
						min++; // Avoid subnet network address
						max--;
					}
					logger.trace("Will scan from {} to {}.", min, max);
					this.logIt(String.format("Will scan %s (from %d to %d)", subnet.getPrefix(),
							min, max), 5);
					List<Integer> existing = session
							.createQuery("select d.mgmtAddress.address from Device d where d.mgmtAddress.address >= :min and d.mgmtAddress.address <= :max")
							.setInteger("min", min)
							.setInteger("max", max)
							.list();
					for (int a = min; a <= max; a++) {
						if (!existing.contains(a)) {
							toScan.add(a);
						}
					}
				}
			}
			catch (HibernateException e) {
				logger.error("Error while retrieving the existing devices in the scope.", e);
				this.logIt("Error while checking the existing devices.", 1);
				this.status = Status.FAILURE;
				return;
			}
			
			try {
				knownCommunities = session
						.createQuery("from DeviceSnmpCommunity c where c.mgmtDomain is null or c.mgmtDomain = :domain")
						.setEntity("domain", domain)
						.list();
			}
			catch (Exception e) {
				logger.error("Error while retrieving the communities.", e);
				this.logIt("Error while getting the communities.", 1);
				this.status = Status.FAILURE;
				return;
			}
		}
		finally {
			session.close();
		}
		if (knownCommunities.size() == 0) {
			logger.error("No available SNMP community to scan devices.");
			this.logIt("No available SNMP community to scan devices.", 1);
			this.status = Status.FAILURE;
			return;
		}
		logger.trace("Will try {} SNMP communities.", knownCommunities.size());
		

		for (int a : toScan) {
			try {
				Network4Address address = new Network4Address(a, 32);
				if (!address.isNormalUnicast()) {
					logger.trace("Bad address {} skipped.", a);
					this.logIt(String.format("Skipping %s.", address.getIp()), 4);
					continue;
				}
				this.logIt("Adding a task to scan " + address.getIp(), 5);
				logger.trace("Will add a discovery task for device with IP {} ({}).", a, address.getIp());
				DiscoverDeviceTypeTask discoverTask = new DiscoverDeviceTypeTask(address, this.getDomain(), comments, author);
				for (DeviceCredentialSet credentialSet : knownCommunities) {
					discoverTask.addCredentialSet(credentialSet);
				}
				TaskManager.addTask(discoverTask);
			}
			catch (Exception e) {
				logger.error("Error while adding discovery task.", e);
				this.logIt("Error while adding discover device type: " + e.getMessage(), 2);
			}
		}
		
		this.status = Status.SUCCESS;
	}
	
	/* (non-Javadoc)
	 * @see onl.netfishers.netshot.work.Task#clone()
	 */
	@Override
  public Object clone() throws CloneNotSupportedException {
	  ScanSubnetsTask task = (ScanSubnetsTask) super.clone();
	  task.setSubnets(this.subnets);
	  return task;
  }

	/**
	 * Gets the domain.
	 *
	 * @return the domain
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	public Domain getDomain() {
		return domain;
	}

	/**
	 * Sets the domain.
	 *
	 * @param domain the new domain
	 */
	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	@ElementCollection(fetch = FetchType.EAGER) @Fetch(FetchMode.SELECT)
	public Set<Network4Address> getSubnets() {
		return subnets;
	}

	public void setSubnets(Set<Network4Address> subnets) {
		this.subnets = subnets;
	}

}


Netshot, a free network management and compliance tool, provided by NetFishers.

For more information, visit http://www.netfishers.onl/netshot
or contact us at netshot@netfishers.onl.



RELEASE HISTORY:

0.5.1 - 2015-10-01

* Ability to select the protocol used over RADIUS to authenticate
  a remote user, using the netshot.aaa.radius.method config line


0.5.0 - 2015-07-14

* Key-based SSH authentication to access devices
* MySQL 5.6 compatibility
* Automatic purge of old configurations using the Purge Database task
* Source IP of improper SNMP traps now displayed in the logs
* 'comment' field in the result of JavaScript rules now properly truncated to 255 characters
* Search toolbox bug fix ([IP address] vs [IP])


0.4.5 - 2015-03-23

This release includes bug fixes only:
* Huawei NE, Fortinet, ASA drivers
* Rule exemptions
* Saving text rules
* Displaying XML configurations

0.4.4 - 2014-12-07

* Fix for the 'dump' feature (dump the last configuration as a file after each
  snapshot on the local system) which never worked in the previous public
  releases.
    This requires a specific option in the Netshot configuration file:
        netshot.snapshots.dump = /path/to/a/local/folder
    The Config attributes with the 'dump' option set to true or to an object
    with additional parameters will be written to the file.


0.4.3a - 2014-11-18

The distribution package now includes a RedHat/CentOS compatible start script.
No change in Netshot itself.
 

0.4.3 - 2014-11-16

The initial public release.

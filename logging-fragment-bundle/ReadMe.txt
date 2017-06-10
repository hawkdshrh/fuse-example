osgi:install -s mvn:org.jboss.fuse.samples/fragment-bundle/1.0

Generate Certificate:
keytool -genkey -dname "CN=Alice, OU=Engineering, O=Progress, ST=Co. Dublin, C=IE" -validity 365 -alias CertAlias -keypass CertPassword -keystore CertName.jks -storepass CertPassword

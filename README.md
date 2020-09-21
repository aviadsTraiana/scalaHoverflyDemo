# scalaHoverflyDemo

## prerequisite

 - certificate and private key under folder 
> demo-hoverfly/src/test/resources/hoverfly/certs/

with names : *my_cert.pem*, *my_key.pem* accordingly.

 

 - Add certificate to java truststore (in certs folder): 
> sudo $JAVA_HOME/bin/keytool -import -alias hoverfly -keystore
> $JAVA_HOME/jre/lib/security/cacerts -file my_cert.pem


the generated certificates are used for bootstapping the mock server in the tests,
and to ble able to request with `OkHttp` we add it to the truststore.

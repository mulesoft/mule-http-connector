# First, we create a private key (a big secret number)
openssl genrsa -out aKey.pem

# Now we need a certificate. A certificate is emmited by a certifying authority (CA) that vouches for us. We need a certificate signing request (or csr)
openssl req -new -key aKey.pem -out certificateSignRequest.csr

# We send the CSR to a CA.

# -----------

# But wait, we don't want to pay so we will just create a fake CA. For that we need another private key first.
openssl genrsa -des3 -out aCA.key 2048

# With the private key of the CA we create a ROOT certificate
openssl req -x509 -new -nodes -key aCA.key -sha256 -days 1825 -out aCA.pem

# Now we can use the root certificate and the private key of the CA to fullfill the CSR
openssl x509 -req -in certificateSignRequest.csr -CA aCA.pem -CAkey aCA.key -CAcreateserial -out X509Certificate.crt -days 365 -sha256

# ------------

#The CA replies with an X509 certificate signed by them. We prefer the PKCS12 format. Lets transform our X509 to a p12
openssl pkcs12 -export -inkey aKey.pem -in X509Certificate.crt -out cert.p12

# Now that I have a certificate signed by the CA we should create a trust store that contains the public key of the CA. This is a mini-database containing the list of CA we trust.
# If someone presents a certificate signed by this CA, we will trust them. This time we will use keytool, which is a java tool and will generate the truststore in a java-specific format called JKS (java key store).
keytool -import -alias aCA -keystore myTruststore.jks -file aCA.pem

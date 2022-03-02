FROM jboss-eap-7.4:alpine
COPY --chown=jboss build/libs/demo03-0.0.1-SNAPSHOT.war /home/jboss/jboss-eap-7.4/standalone/deployments/demo03.war

ENV USE_JNDI "true"
ENV JNDI "java:jboss/datasources/demo03"
ENV URL "jdbc:postgresql://demo03-db/demo03"
ENV DB_USER "demo03"
ENV DB_PASS "Zaq12wsx."
ENV SPRING_PROFILES_ACTIVE: "jboss"

ENTRYPOINT ["/install.sh", "-c"]
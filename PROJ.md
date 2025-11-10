## Estos son mis repositorios

### 1. Con este despliego la base de datos persistente (Postgres).
https://github.com/jnsierra/proj-openshift-postgres

### 2. Con este despliego el microservicio que se conecta a la base de datos y expone los servicios REST (Quarkus).
https://github.com/jnsierra/proj-openshift-db

### 3. Con este despliego la aplicacion web con REACT. Con la cual puedo ver los datos e insertar registros.
https://github.com/jnsierra/proj-openshift-web

### 4. Con este proyecto creo el chart de Helm.
https://github.com/jnsierra/proj-openshift-db-chart

Añadi dos politicas una para que la aplicacion Web llegará a al microservicio

Esta es la ruta del app: 

http://proj-openshift-web-proj-openshift-chart-nicolas-sierra-dev.apps.rm1.0a51.p1.openshiftapps.com/

Politica para permitir el trafico entre pods en todo el namespace:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-all-namespaces
  namespace: NOMBRE_DEL_NAMESPACE
spec:
  podSelector: {}
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - namespaceSelector: {}   # permite desde cualquier namespace
  egress:
    - to:
        - namespaceSelector: {}   # permite hacia cualquier namespace
```
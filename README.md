# Spring Cloud Function - SpEL Injection (CVE-2022-22963)

```
cd spring-cloud-function-samples/function-sample-pojo && mvn clean package -DskipTests && java -jar target/function-sample-pojo-2.0.0.RELEASE.jar
```

```
codeql database create spring-cloud-function-3.2.X-DB -l java -j0 --search-path /path/to/codeql -c "./mvnw compile -P all -DskipTests -Dos.arch=x86_64"
```

Fix commit: https://github.com/spring-cloud/spring-cloud-function/commit/dc5128b80c6c04232a081458f637c81a64fa9b52

### Research

* https://mp.weixin.qq.com/s/ssHcLC72wZqzt-ei_ZoLwg
* https://hosch3n.github.io/2022/03/26/SpringCloudFunction%E6%BC%8F%E6%B4%9E%E5%88%86%E6%9E%90/
* https://mp.weixin.qq.com/s/U7YJ3FttuWSOgCodVSqemg
* https://github.com/lunasec-io/lunasec/blob/master/docs/blog/2022-03-30-spring-core-rce.mdx#cve-2022-22963

### POCs

* https://github.com/hktalent/spring-spel-0day-poc
* https://github.com/RanDengShiFu/CVE-2022-22963
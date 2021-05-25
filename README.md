Udacity and Twitter bring you Real-Time Analytics with Apache Storm
=====

Join the course for free:
www.udacity.com/course/ud381

Since maven has enforced new ssl policy

## settings.xml
We need to add a settings.xml to use http instead of https

`vi /home/vagrant/.m2/settings.xml`
<pre>
&lt;settings&gt;
    &lt;mirrors&gt;
        &lt;mirror&gt;
            &lt;id&gt;centralhttps&lt;/id&gt;
            &lt;mirrorOf>central&lt;/mirrorOf&gt;
            &lt;name&gt;Maven central https&lt;/name&gt;
            &lt;url&gt;http://insecure.repo1.maven.org/maven2/&lt;/url&gt;
        &lt;/mirror&gt;
    &lt;/mirrors&gt;
&lt;/settings&gt;
</pre>

## New storm build command
to avoid peer not authenticated error

`
mvn package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dhttps.protocols=TLSv1.2
`


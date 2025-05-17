package share_app.tphucshareapp.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@Slf4j
public class MongoDBConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;

    @Value("${spring.data.mongodb.port:27017}")
    private int port;

    @Value("${spring.data.mongodb.connections-per-host:100}")
    private int connectionsPerHost;

    @Value("${spring.data.mongodb.min-connections-per-host:10}")
    private int minConnectionsPerHost;

    @Value("${spring.data.mongodb.max-idle-time-ms:60000}")
    private int maxIdleTimeMs;

    @Value("${spring.data.mongodb.connect-timeout:30000}")
    private int connectTimeout;

    @Value("${spring.data.mongodb.socket-timeout:60000}")
    private int socketTimeout;

    @Value("${spring.data.mongodb.server-selection-timeout:30000}")
    private int serverSelectionTimeout;

    @Override
    public MongoClient mongoClient() {
        log.info("Configuring MongoDB connection with host: {}, port: {}", host, port);

        // Tạo danh sách các server address (cho phép cấu hình nhiều replica nếu cần)
        List<ServerAddress> serverAddresses = new ArrayList<>();
        serverAddresses.add(new ServerAddress(host, port));

        // Nếu có nhiều replica node, thêm vào danh sách:
        // serverAddresses.add(new ServerAddress("replica1.example.com", 27017));
        // serverAddresses.add(new ServerAddress("replica2.example.com", 27017));

        // Cấu hình MongoDB cluster settings
        ClusterSettings clusterSettings = ClusterSettings.builder()
                .hosts(serverAddresses)
                .mode(ClusterConnectionMode.MULTIPLE) // Mode cho phép kết nối đến nhiều node
                .build();

        // Cấu hình MongoDB client settings
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.applySettings(clusterSettings))
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(connectionsPerHost)
                        .minSize(minConnectionsPerHost)
                        .maxConnectionIdleTime(maxIdleTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(connectTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .readTimeout(socketTimeout, java.util.concurrent.TimeUnit.MILLISECONDS))
                .applyToServerSettings(builder -> builder
                        .heartbeatFrequency(10000, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .minHeartbeatFrequency(500, java.util.concurrent.TimeUnit.MILLISECONDS))
                .applyToClusterSettings(builder -> builder
                        .serverSelectionTimeout(serverSelectionTimeout, java.util.concurrent.TimeUnit.MILLISECONDS))
                .build();

        // Tạo và trả về MongoDB client với cấu hình tùy chỉnh
        return MongoClients.create(clientSettings);
    }

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return List.of("share_app.tphucshareapp.model");
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() throws Exception {
        MappingMongoConverter converter = new MappingMongoConverter(mongoDbFactory(), new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null)); // Loại bỏ _class field

        return new MongoTemplate(mongoDbFactory(), converter);
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}

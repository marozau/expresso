package today.expresso.stream.serde.generic;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Collections;
import java.util.Map;

public class GenericAvroSerde implements Serde<Object> {

	private final Serde<Object> inner;

	/**
	 * Constructor used by Kafka Streams.
	 */
	public GenericAvroSerde() {
		inner = Serdes.serdeFrom(new GenericAvroSerializer(), new GenericAvroDeserializer());
	}

	public GenericAvroSerde(SchemaRegistryClient client) {
		this(client, Collections.emptyMap());
	}

	public GenericAvroSerde(SchemaRegistryClient client, Map<String, ?> props) {
		inner = Serdes.serdeFrom(new GenericAvroSerializer(client), new GenericAvroDeserializer(client, props));
	}

	public GenericAvroSerde(Map<String, ?> props) {
		this(instantiateSchemaRegistryClient(props), props);
	}

	private static SchemaRegistryClient instantiateSchemaRegistryClient(Map<String, ?> props) {
		Object url = props.get(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG);
		return new CachedSchemaRegistryClient(
				(String) url, AbstractKafkaAvroSerDeConfig.MAX_SCHEMAS_PER_SUBJECT_DEFAULT);
	}

	@Override
	public Serializer<Object> serializer() {
		return inner.serializer();
	}

	@Override
	public Deserializer<Object> deserializer() {
		return inner.deserializer();
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		inner.serializer().configure(configs, isKey);
		inner.deserializer().configure(configs, isKey);
	}

	@Override
	public void close() {
		inner.serializer().close();
		inner.deserializer().close();
	}

}
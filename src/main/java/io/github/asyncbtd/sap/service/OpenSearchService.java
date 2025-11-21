package io.github.asyncbtd.sap.service;

import io.github.asyncbtd.sap.config.prop.OpenSearchProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchService {

    private final OpenSearchClient client;
    private final OpenSearchProps props;

    @PostConstruct
    public void initIndex() {
        try {
            String indexName = props.getIndexName();
            ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
            
            if (!client.indices().exists(existsRequest).value()) {
                CreateIndexRequest createIndexRequest = CreateIndexRequest.of(i -> i
                        .index(indexName)
                        .settings(s -> s
                                .analysis(a -> a
                                        .analyzer("russian_morphology", analyzer -> analyzer
                                                .custom(custom -> custom
                                                        .tokenizer("standard")
                                                        .filter("lowercase", "stop")
                                                )
                                        )
                                        .analyzer("english_morphology", analyzer -> analyzer
                                                .custom(custom -> custom
                                                        .tokenizer("standard")
                                                        .filter("lowercase", "stop", "english_stemmer")
                                                )
                                        )
                                )
                        )
                        .mappings(m -> m
                                .properties("id", p -> p
                                        .text(t -> t.index(false))
                                )
                                .properties("description", p -> p
                                        .text(TextProperty.of(t -> t
                                                .analyzer("standard")
                                                .searchAnalyzer("standard")
                                                .fields("russian", f -> f
                                                        .text(t2 -> t2
                                                                .analyzer("russian_morphology")
                                                                .searchAnalyzer("russian_morphology")
                                                        )
                                                )
                                                .fields("english", f -> f
                                                        .text(t2 -> t2
                                                                .analyzer("english_morphology")
                                                                .searchAnalyzer("english_morphology")
                                                        )
                                                )
                                        ))
                                )
                        )
                );
                client.indices().create(createIndexRequest);
                log.info("Created index: {}", indexName);
            } else {
                log.debug("Index already exists: {}", indexName);
            }
        } catch (IOException e) {
            log.error("Failed to initialize index", e);
        }
    }

    public void indexImage(UUID id, String description) {
        try {
            IndexRequest<Map<String, String>> request = IndexRequest.of(i -> i
                    .index(props.getIndexName())
                    .id(id.toString())
                    .document(Map.of(
                            "id", id.toString(),
                            "description", description
                    ))
            );

            client.index(request);
            log.debug("Indexed image with id: {}", id);
        } catch (IOException e) {
            log.error("Failed to index image", e);
        }
    }

    public List<UUID> searchByDescription(String query) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(props.getIndexName())
                    .query(q -> q
                            .bool(b -> b
                                    // Поиск по русскому полю с морфологией
                                    .should(should -> should
                                            .match(m -> m
                                                    .field("description.russian")
                                                    .query(FieldValue.of(query))
                                                    .fuzziness("AUTO")
                                                    .boost(3.0f)
                                            )
                                    )
                                    // Поиск по английскому полю с морфологией
                                    .should(should -> should
                                            .match(m -> m
                                                    .field("description.english")
                                                    .query(FieldValue.of(query))
                                                    .fuzziness("AUTO")
                                                    .boost(3.0f)
                                            )
                                    )
                                    // Поиск по основному полю (fallback)
                                    .should(should -> should
                                            .match(m -> m
                                                    .field("description")
                                                    .query(FieldValue.of(query))
                                                    .fuzziness("AUTO")
                                                    .boost(2.0f)
                                            )
                                    )
                                    // Точное совпадение фраз
                                    .should(should -> should
                                            .matchPhrase(m -> m
                                                    .field("description.russian")
                                                    .query(query)
                                                    .boost(5.0f)
                                            )
                                    )
                                    .should(should -> should
                                            .matchPhrase(m -> m
                                                    .field("description.english")
                                                    .query(query)
                                                    .boost(5.0f)
                                            )
                                    )
                                    // Минимальное требование: хотя бы одно условие должно сработать
                                    .minimumShouldMatch("1")
                            )
                    )
            );

            SearchResponse<Map> response = client.search(searchRequest, Map.class);

            return response.hits().hits().stream()
                    .map(hit -> UUID.fromString(hit.id()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Search failed", e);
            return List.of();
        }
    }

    public void deleteImage(UUID id) {
        try {
            DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                    .index(props.getIndexName())
                    .id(id.toString())
            );
            client.delete(deleteRequest);
            log.debug("Deleted image from index: {}", id);
        } catch (IOException e) {
            log.error("Failed to delete image from index", e);
        }
    }
}

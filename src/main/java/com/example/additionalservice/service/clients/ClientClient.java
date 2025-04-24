package com.example.additionalservice.service.clients;

import com.example.additionalservice.model.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class ClientClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ClientClient(RestTemplate restTemplate,
                        @Value("${main.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Получает список всех клиентов из основного сервиса
     * @return список клиентов или пустой список, если произошла ошибка
     */
    public List<Client> getAllClients() {
        String url = baseUrl + "/clients";
        Client[] clients = restTemplate.getForObject(url, Client[].class);
        return clients != null ? Arrays.asList(clients) : List.of();
    }

    /**
     * Получает клиента по ID
     * @param id идентификатор клиента
     * @return объект клиента или null, если не найден
     */
    public Client getClientById(Long id) {
        String url = baseUrl + "/clients/" + id;
        return restTemplate.getForObject(url, Client.class);
    }

    /**
     * Создает нового клиента
     * @param client данные клиента
     * @return созданный клиент
     */
    public Client createClient(Client client) {
        String url = baseUrl + "/clients";
        return restTemplate.postForObject(url, client, Client.class);
    }

    /**
     * Обновляет данные клиента
     * @param id идентификатор клиента
     * @param client обновленные данные
     */
    public void updateClient(Long id, Client client) {
        String url = baseUrl + "/clients/" + id;
        restTemplate.put(url, client);
    }

    /**
     * Удаляет клиента
     * @param id идентификатор клиента
     */
    public void deleteClient(Long id) {
        String url = baseUrl + "/clients/" + id;
        restTemplate.delete(url);
    }

    /**
     * Получает клиентов по номеру водительских прав
     * @param license номер прав
     * @return список клиентов с указанными правами
     */
    public List<Client> getClientsByDriverLicense(String license) {
        String url = baseUrl + "/clients?driverLicense=" + license;
        Client[] clients = restTemplate.getForObject(url, Client[].class);
        return clients != null ? Arrays.asList(clients) : List.of();
    }
}

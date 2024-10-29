package io.dapr.kubecon.examples.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContentDecoder {

    // generated via https://json-generator.com/
    private static final String FAKE_PAYLOAD = """
            [
              {
                "_id": "671f90067f196514a90bc4c6",
                "index": 0,
                "guid": "d9627054-f0bf-4977-954d-b771094404ba",
                "isActive": false,
                "balance": "$3,041.91",
                "picture": "http://placehold.it/32x32",
                "age": 22,
                "eyeColor": "brown",
                "name": "Maude Walter",
                "gender": "female",
                "company": "BIOLIVE",
                "email": "maudewalter@biolive.com",
                "phone": "+1 (963) 537-2667",
                "address": "651 Etna Street, Mahtowa, Rhode Island, 6493",
                "about": "Quis ex et do elit non dolore. Qui aute sunt dolor duis. Velit veniam ut incididunt consectetur adipisicing esse magna. Consequat mollit eu voluptate nostrud. Excepteur nulla mollit ex dolor officia. Laborum ad adipisicing dolore veniam incididunt dolor esse dolor eu aliquip consectetur laboris. Do ex proident voluptate nostrud sunt aute culpa minim labore eiusmod minim non et.\\r\\n",
                "registered": "2017-12-11T01:32:13 -01:00",
                "latitude": 54.68552,
                "longitude": -177.822882,
                "tags": [
                  "Lorem",
                  "consequat",
                  "officia",
                  "do",
                  "pariatur",
                  "quis",
                  "quis"
                ],
                "friends": [
                  {
                    "id": 0,
                    "name": "Mooney Greene"
                  },
                  {
                    "id": 1,
                    "name": "Margery Hampton"
                  },
                  {
                    "id": 2,
                    "name": "Glover Sanchez"
                  }
                ],
                "greeting": "Hello, Maude Walter! You have 4 unread messages.",
                "favoriteFruit": "apple"
              },
              {
                "_id": "671f90065358d3c7c615a8e8",
                "index": 1,
                "guid": "be67855d-6ed2-49e3-8339-40694557a066",
                "isActive": true,
                "balance": "$2,492.67",
                "picture": "http://placehold.it/32x32",
                "age": 21,
                "eyeColor": "brown",
                "name": "Rivas Byers",
                "gender": "male",
                "company": "XERONK",
                "email": "rivasbyers@xeronk.com",
                "phone": "+1 (922) 583-2611",
                "address": "104 Ditmars Street, Cumminsville, Arkansas, 6788",
                "about": "Cillum tempor ut non fugiat dolor ullamco exercitation duis irure qui anim laborum labore. Cillum velit duis magna pariatur incididunt velit amet fugiat pariatur elit consectetur dolore. Fugiat fugiat consectetur sit consectetur excepteur eiusmod exercitation do. Eiusmod tempor commodo tempor elit consectetur incididunt sint. Lorem aliquip et et dolore consectetur aliqua.\\r\\n",
                "registered": "2023-05-30T03:49:45 -02:00",
                "latitude": -88.917101,
                "longitude": 54.702665,
                "tags": [
                  "ut",
                  "eu",
                  "commodo",
                  "ea",
                  "duis",
                  "aliquip",
                  "magna"
                ],
                "friends": [
                  {
                    "id": 0,
                    "name": "Merrill Mendoza"
                  },
                  {
                    "id": 1,
                    "name": "Curry Mccarty"
                  },
                  {
                    "id": 2,
                    "name": "Lee Dale"
                  }
                ],
                "greeting": "Hello, Rivas Byers! You have 10 unread messages.",
                "favoriteFruit": "apple"
              },
              {
                "_id": "671f9006f15ab7cd63ebda44",
                "index": 2,
                "guid": "668c5867-cdae-471c-99ab-385d3cad9d8d",
                "isActive": false,
                "balance": "$3,705.15",
                "picture": "http://placehold.it/32x32",
                "age": 25,
                "eyeColor": "blue",
                "name": "Compton Hood",
                "gender": "male",
                "company": "VORTEXACO",
                "email": "comptonhood@vortexaco.com",
                "phone": "+1 (991) 591-3344",
                "address": "277 Heyward Street, Iola, Tennessee, 495",
                "about": "Duis irure consequat mollit eiusmod occaecat laboris ipsum. Sunt Lorem minim eu in consectetur nulla dolor labore laborum. Ex veniam reprehenderit esse reprehenderit consequat sint non irure non non aliqua excepteur do nostrud. Sunt velit in ad commodo et sint ex velit cillum.\\r\\n",
                "registered": "2014-10-09T08:17:48 -02:00",
                "latitude": -71.634356,
                "longitude": 129.87354,
                "tags": [
                  "velit",
                  "irure",
                  "minim",
                  "proident",
                  "mollit",
                  "sunt",
                  "aute"
                ],
                "friends": [
                  {
                    "id": 0,
                    "name": "Wheeler Hoffman"
                  },
                  {
                    "id": 1,
                    "name": "Clara Hicks"
                  },
                  {
                    "id": 2,
                    "name": "Potter Mcpherson"
                  }
                ],
                "greeting": "Hello, Compton Hood! You have 4 unread messages.",
                "favoriteFruit": "strawberry"
              }
            ]
            """;

    private static volatile Object sink;

    public static void deserialize() {
        long start = System.nanoTime();
        while ((System.nanoTime() - start) < 20_000_000) {
            //Extra anti-pattern: recreate ObjectMapper everytime
            ObjectMapper mapper = new ObjectMapper();
            try {
                sink = mapper.readTree(FAKE_PAYLOAD);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

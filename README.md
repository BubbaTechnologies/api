# Bubba Technologies Inc. API
Updated 05/12/2023

## Generic Controller
### /

Mapping: GET

Description: Returns to homepage (*https://www.peachsconemarket.com*)

### /error

Mapping: GET

Description: Returns 400 http code

### /health

Mapping: GET

Description: Returns 200 code.

### /login

Mapping: POST

Description: Login with user credentials and receive a jwt back.

Request Body: 
```
{
    "username": str,
    "password": str,
}
```

Response: 
```
{
    "jwt":str, 
    "name":str, 
    "username":str
}
```

### /create

Mapping: POST

Description: Create user. If user exists, response code will be 400.

Request Body:
```
{
    "username":str, 
    "password":str, 
    "gender":str, 
    "name":str
}
```

Response: 
```
{
    "jwt":str, 
    "name":str, 
    "username":str
}
```


### /update

Mapping: PUT

Required Headers: ```Authorization: Bearer (JWT)```

Description: Update user details.

Request Body: 
```
{
    "username":str, 
    "password":str, 
    "gender":str, 
    "name":str
}
```

Response: 
```
{
    "id":int,
    "username":str,
    "password":str,
    "likes":[LIKES],
    "gender":str,
    "enabled":bool,
    "name":str
}
```


### /delete

Mapping: DELETE

Required Headers: ```Authorization: Bearer (JWT)```

Description: Deletes User. Returns HTTP status code 200 if successful.

## App Controller
### /app/card

Mapping: GET

Request Parameters: type *(OPTIONAL)*, gender *(OPTIONAL)*

Description: Returns clothing card for user.

Required Headers: ```Authorization: Bearer (JWT)```

Response:
```
{
    "id":int,
    "name":str,
    "imageURL": [str],
    "productURL":str,
    "store": {
        "id":int,
        "name":str,
        "url":str
    },
    "type":str,
    "gender": [str],
    "_links": {
        "self": {
            "href": "http://localhost:8080/app/card{?type,gender}",
            "templated": true
        },
        "createLike": {
            "href": "http://localhost:8080/app/like"
        },
        "createLove": {
            "href": "http://localhost:8080/app/like"
        }
    }
}
```

### /app/likes
Mapping: GET

Description: Returns all likes for user.

Required Headers: ```Authorization: Bearer (JWT)```

Response:

```
{
    "_embedded": {
        "clothingDTOList": [
            {
                "id":int,
                "name":str,
                "imageURL": [str],
                "productURL": str,
                "store": {
                    "id": int,
                    "name": str,
                    "url": str
                },
                "type": str,
                "gender": [str]
            }
        ]
    },
    "_links": {
        "self": {
            "href": "http://localhost:8080/app/likes"
        },
        "card": {
            "href": "http://localhost:8080/app/card{?type,gender}",
            "templated": true
        }
    }
}
```

### /app/collection
Mapping: GET

Description: Returns all loved items for user.

Required Headers: ```Authorization: Bearer (JWT)```

Response:

```
{
    "_embedded": {
        "clothingDTOList": [
            {
                "id":int,
                "name":str,
                "imageURL": [str],
                "productURL": str,
                "store": {
                    "id": int,
                    "name": str,
                    "url": str
                },
                "type": str,
                "gender": [str]
            }
        ]
    },
    "_links": {
        "self": {
            "href": "http://localhost:8080/app/likes"
        },
        "card": {
            "href": "http://localhost:8080/app/card{?type,gender}",
            "templated": true
        }
    }
}
```

### /app/like (POST)
Mapping: POST

Required Headers: ```Authorization: Bearer (JWT)```

Description: Creates like.

Request:

```
{
    "clothingId":str,
    "rating":str
}
```

Response:
```
{
    "id":int,
    "rating":int
}
```
### /app/like (DELETE)
Mapping: DELETE

Request Parameters: clothingId

Description: Deletes like.

Required Headers: ```Authorization: Bearer (JWT)```


### /app/like (PUT)
Mapping: PUT

Required Headers: ```Authorization: Bearer (JWT)```

Description: Updates like.

Request:

```
{
    "clothingId":str,
    "rating":str
}
```

Response:
```
{
    "id":int,
    "rating":int
}
```

## Scraper Controller

### /scraper/checkStore
(DEPRECIATED: Use /scraper/store)

Mapping: GET

Request Parameter: url

Required Header: ```Authorization: Bearer (JWT)```

Description: Check if store exists in database by URL.

Response: If store does not exist, 
```
{}
``` 
else,

```
{
    "id":int,
    "name":str,
    "url":str
}
```

### /scraper/checkClothing

Mapping: GET

Request Parameter: url

Required Header: ```Authorization: Bearer (JWT)```

Description: Check if clothing exists in database by URL.

Response: If clothing does not exist,
```
{}
```
else,
```
{
    "id":int,
    "name":str,
    "imageURL": [str],
    "productURL":str,
    "store": {
        "id":int,
        "name":str,
        "url":str
    },
    "type":str,
    "gender": [str]
}
```

### /scraper/store

Mapping: POST

Required Header: ```Authorization: Bearer (JWT)```

Description: If store exists, returns store info. Creates a new store otherwise.

Request Body:
```
{
    "name":str,
    "url":str
}
```

Response:
```
{
    "id":int,
    "name":str,
    "url":str
}
```



### /scraper/clothing
Mapping: POST

Required Header: ```Authorization: Bearer (JWT)```

Description: Create new clothing item.

Request Body:
```
{
    "name":str,
    "imageUrl":[str],
    "productUrl":str,
    "storeId":str,
    "type":str,
    "gender":str
}
```

Response:
```
{
    "id":int,
    "name":str,
    "imageURL": [str],
    "productURL":str,
    "store": {
        "id":int,
        "name":str,
        "url":str
    },
    "type":str,
    "gender": [str]
}
```

## AI Controller
**TODO**






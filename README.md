# Bubba Technologies Inc. API
Updated 05/17/2023

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
    "imageURL":[str],
    "productURL":str,
    "store": {
        "id":int,
        "name":str,
        "url":str
    },
    "type":str,
    "gender":str,
    "date":str
}
```

### /app/cardList

Mapping: GET

Request Parameters: type *(OPTIONAL)*, gender *(OPTIONAL)*

Description: Returns a list of clothing for user.

Required Headers: ```Authorization: Bearer (JWT)```

Response:
```
{
    "_embedded":{
        "clothingDTOList":[
            "id":int,
            "name":str,
            "imageURL":[str],
            "productURL":str,
            "store": {
                "id":int,
                "name":str,
                "url":str
            },
            "type":str,
            "gender":str,
            "date":str
        ]
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
                "imageURL":[str],
                "productURL":str,
                "store": {
                    "id":int,
                    "name":str,
                    "url":str
                },
                "type":str,
                "gender":str,
                "date":str
            }
        ]
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
                "gender": str,
                "date":str
            }
        ]
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
    "imageTapRatio":double,
    "clothingId":int
}
```

Response:
```
{
    "id": int,
    "date": string,
    "liked": bool,
    "bought": bool
}
```

### /app/dislike (POST)
Mapping: POST

Required Headers: ```Authorization: Bearer (JWT)```

Description: Creates dislike.

Request:

```
{
    "imageTapRatio":double,
    "clothingId":int
}
```

Response:
```
{
    "id": int,
    "date": string,
    "liked": bool,
    "bought": bool
}
```

### /app/removeLike (POST)
Mapping: POST

Required Headers: ```Authorization: Bearer (JWT)```

Description: Removes like.

Request:

```
{
    "imageTapRatio":double,
    "clothingId":int
}
```

Response:
```
{
    "id": int,
    "date": string,
    "liked": bool,
    "bought": bool
}
```

### /app/bought (POST)
Mapping: POST

Required Headers: ```Authorization: Bearer (JWT)```

Description: Updates like to bought.

Request:

```
{
    "imageTapRatio":double,
    "clothingId":int
}
```

Response:
```
{
    "id": int,
    "date": string,
    "liked": bool,
    "bought": bool
}
```

### /app/pageClick (POST)
Mapping: POST

Required Headers: ```Authorization: Bearer (JWT)```

Description: Updates like to page click.

Request:

```
{
    "imageTapRatio":double,
    "clothingId":int
}
```

Response:
```
{
    "id": int,
    "date": string,
    "liked": bool,
    "bought": bool
}
```

### /app/filterOptions
Mapping: GET

Required Headers: ```Authorization: Bearer (JWT)```

Description: Returns genders and types per gender.

Response:
```
{
    "genders":[str],
    "types":[[str]]
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
    "imageURL":[str],
    "productURL":str,
    "store": {
        "id":int,
        "name":str,
        "url":str
    },
    "type": str,
    "gender":[str],
    "date":str
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






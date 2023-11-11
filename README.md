# Bubba Technologies Inc. API
Updated 07/23/2023

## Environment Variables

- ```DB_ADDR_WRITER```: Address for database with write permission.
- ```DB_USERNAME```: Username for database.
- ```DB_PASSWORD```: Password for database.
- ```DB_PORT```: Port for database.
- ```SERVER_PORT```: Port for the API to accept incoming communications.
- ```RECOMMENDATION_ADDR```: Recommendation service address.
- ```IMAGE_ADDR```: Image processing service address.
- ```JWT_SECRET```: Key used to generate JWT tokens.

## Properties
(NOTE: Only included most important properties)
- ```spring.jpa.hibernate.ddl-auto```: Determines how API interacts with database upon start.

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
### /verify

Mapping: POST

Description: Sends verification code to users email.

Request Body:
```
{
    "email":str
}
```


Response: 200 if sent.

### /create

Mapping: POST

Parameters: ```code: str``` 

Description: Create user. If user exists, response code will be 400.

Request Body:
```
{
    "username":str, 
    "password":str, 
    "gender":str, 
    "name":str,
    "birthdate":str
}
```
NOTE: Birthdate must be formatted as follows: YEAR-MONTH-DAY such that December 18th, 2001 is
2001-12-18.


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
    "date":str,
    "tags":[str]
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
            "date":str,
            "tags":[str]
        ]
    }
}
```
### /app/checkToken
Mapping: GET

Description: Returns 200 if valid user and updates last login.

Required Headers: ```Authorization: Bearer (JWT)```

### /app/totalPage
Mapping: GET

Description: Returns all likes for user.

Required Headers: ```Authorization: Bearer (JWT)```

Parameters:

**type (OPTIONAL)**

Description: Filters return list by clothing type.

**gender (OPTIONAL)**

Description: Filters return list by gender.

**page (OPTIONAL)**

Description: Returns list in pages that are predefined sizes.

**queryType**

Description: Defines type of query.
Values: `likes` or `collection`.

Response:

```
{
    "totalPages":Long
}
```

### /app/likes
Mapping: GET

Description: Returns all likes for user.

Required Headers: ```Authorization: Bearer (JWT)```

Parameters:

**type (OPTIONAL)**

Description: Filters return list by clothing type.

**gender (OPTIONAL)**

Description: Filters return list by gender.

**page (OPTIONAL)**

Description: Returns list in pages that are predefined sizes.

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

Parameters:

**type (OPTIONAL)**

Description: Filters return list by clothing type.

**gender (OPTIONAL)**

Description: Filters return list by gender.

**page (OPTIONAL)**

Description: Returns list in pages that are predefined sizes.

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

Description: Updates like to bought state.

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

Description: Returns genders, types, and tags to filter by.

Response:
```
{
    "genders":[str],
    "types":[[str]],
    "tags": {
        type:[str]
    }
}
```


Each string array in type with correspond to the gender with the same index. Tags will be formatted with the key being a type and the string array containing the corresponding tags. 
e.g. 
```
top:[active]
```

### /app/updateLocation
Mapping: POST

Description: Updates a users location.

Request:
```
{
    "latitude":Double,
    "longitude":Double
}
```

Response: If requests succeed 200.

### /app/updateDeviceId
Mapping: POST

Description: Updates a users location.

Request:
```
{
    "deviceId":String
}
```

Response: If requests succeed 200.

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
    "date":str,
    "tags":[str]
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

### /scraper/store

Mapping: GET

Request Parameter: ```storeName: str```

Required Header: ```Authorization: Bearer (JWT)```

Description: If store exists, returns the amount of clothing collected within the last week. 
Otherwise, returns a 404 status code.

Response:
```
{
    "lastWeekCollections":long
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
    "gender":str,
    "tags":[str]
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
    "gender":[str],
    "tags":[str]
}
```

## Admin Controller
### /admin/routeResponseTime
Mapping: GET
Required Header: ```Authorization: Bearer (JWT)```
Description: Returns JSON response containing average response times for /app routes.







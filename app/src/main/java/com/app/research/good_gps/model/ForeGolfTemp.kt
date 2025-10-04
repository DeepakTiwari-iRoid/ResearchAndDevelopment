package com.app.research.good_gps.model

import com.app.research.utils.AppUtils.fromJsonString

object ForeGolfTemp {


    private val clubsJson = """{
  "apiRequestsLeft": "777",
  "numClubs": 5,
  "numAllClubs": 5,
  "clubs": [
    {
      "clubID": "141520610397251566",
      "clubName": "Pebble Beach Golf Links",
      "city": "Pebble Beach",
      "state": "CA",
      "country": "USA",
      "address": "1700 17 Mile Drive",
      "timestampUpdated": "1704550359",
      "distance": 0.062,
      "measureUnit": "km",
      "courses": [
        {
          "courseID": "012141520658891108829",
          "courseName": "Pebble Beach",
          "numHoles": 18,
          "timestampUpdated": "1704549296",
          "hasGPS": 1
        },
        {
          "courseID": "011141520658921417047",
          "courseName": "The Hay",
          "numHoles": 9,
          "timestampUpdated": "1672862025",
          "hasGPS": 0
        }
      ]
    },
    {
      "clubID": "141520610413056394",
      "clubName": "Spyglass Hill Golf Course",
      "city": "Pebble Beach",
      "state": "CA",
      "country": "USA",
      "address": "Spyglass Hill Rd & Stevenson Dr",
      "timestampUpdated": "1704548446",
      "distance": 1.103,
      "measureUnit": "km",
      "courses": [
        {
          "courseID": "012141520665343800921",
          "courseName": "18-hole course",
          "numHoles": 18,
          "timestampUpdated": "1704548446",
          "hasGPS": 1
        }
      ]
    },
    {
      "clubID": "141520610398953838",
      "clubName": "Poppy Hills Golf Course",
      "city": "Pebble Beach",
      "state": "CA",
      "country": "USA",
      "address": "3200 Lopez Rd",
      "timestampUpdated": "1704548040",
      "distance": 1.254,
      "measureUnit": "km",
      "courses": [
        {
          "courseID": "0121346869355741",
          "courseName": "18-hole course",
          "numHoles": 18,
          "timestampUpdated": "1704548040",
          "hasGPS": 1
        }
      ]
    },
    {
      "clubID": "141520610366390646",
      "clubName": "Cypress Point Club",
      "city": "Pebble Beach",
      "state": "CA",
      "country": "USA",
      "address": "3150 17 Mile Drive",
      "timestampUpdated": "1704550537",
      "distance": 1.594,
      "measureUnit": "km",
      "courses": [
        {
          "courseID": "012141520647281224131",
          "courseName": "Cypress Point",
          "numHoles": 18,
          "timestampUpdated": "1679922041",
          "hasGPS": 1
        }
      ]
    },
    {
      "clubID": "141520610418383036",
      "clubName": "The Links at Spanish Bay",
      "city": "Pebble Beach",
      "state": "CA",
      "country": "USA",
      "address": "2700 17 Mile Drive",
      "timestampUpdated": "1704550515",
      "distance": 3.097,
      "measureUnit": "km",
      "courses": [
        {
          "courseID": "0121130522396078",
          "courseName": "18-hole course",
          "numHoles": 18,
          "timestampUpdated": "1704548785",
          "hasGPS": 1
        }
      ]
    }
  ]
}""".trimIndent()
    private val clubJson = """{
  "clubID": "141520610397251566",
  "clubName": "Pebble Beach Golf Links",
  "city": "Pebble Beach",
  "state": "CA",
  "country": "USA",
  "address": "1700 17 Mile Drive",
  "postalCode": "93953",
  "latitude": "36.584532",
  "longitude": "-121.964661",
  "website": "www.pebblebeach.com",
  "telephone": "(800) 654-9300",
  "timestampUpdated": "1672862167",
  "courses": [
    {
      "courseID": "012141520658891108829",
      "courseName": "Pebble Beach",
      "numHoles": 18,
      "timestampUpdated": "1672862167",
      "hasGPS": 1
    },
    {
      "courseID": "011141520658921417047",
      "courseName": "The Hay",
      "numHoles": 9,
      "timestampUpdated": "1672862025",
      "hasGPS": 0
    }
  ],
  "apiRequestsLeft": "954"
}""".trimIndent()
    private val courseJson = """{
  "clubID": "141520610397251566",
  "clubName": "Pebble Beach Golf Links",
  "address": "1700 17 Mile Drive",
  "postalCode": "93953",
  "city": "Pebble Beach",
  "state": "CA",
  "country": "USA",
  "latitude": "36.5696553",
  "longitude": "-121.9497555",
  "website": "https://www.pebblebeach.com/golf/pebble-beach-golf-links",
  "telephone": "+1 831-574-5609",
  "courseID": "012141520658891108829",
  "courseName": "Pebble Beach",
  "numHoles": "18",
  "timestampUpdated": "1704549296",
  "hasGPS": "1",
  "measure": "y",
  "parsMen": [
    4,
    5,
    4,
    4,
    3,
    5,
    3,
    4,
    4,
    4,
    4,
    3,
    4,
    5,
    4,
    4,
    3,
    5
  ],
  "indexesMen": [
    6,
    10,
    12,
    16,
    14,
    2,
    18,
    4,
    8,
    3,
    9,
    17,
    7,
    1,
    13,
    11,
    15,
    5
  ],
  "parsWomen": [
    4,
    5,
    4,
    4,
    3,
    5,
    3,
    4,
    4,
    4,
    4,
    3,
    4,
    5,
    4,
    4,
    3,
    5
  ],
  "indexesWomen": [
    10,
    6,
    12,
    14,
    16,
    4,
    18,
    2,
    8,
    7,
    9,
    13,
    11,
    1,
    15,
    5,
    17,
    3
  ],
  "numTees": 5,
  "tees": [
    {
      "teeID": "106671",
      "teeName": "Blue",
      "teeColor": "#00CCFF",
      "length1": 378,
      "length2": 509,
      "length3": 397,
      "length4": 333,
      "length5": 189,
      "length6": 498,
      "length7": 107,
      "length8": 416,
      "length9": 483,
      "length10": 444,
      "length11": 370,
      "length12": 202,
      "length13": 401,
      "length14": 559,
      "length15": 393,
      "length16": 400,
      "length17": 182,
      "length18": 541,
      "courseRatingMen": 74.9,
      "slopeMen": 144,
      "courseRatingWomen": "",
      "slopeWomen": "",
      "courseRatingMenFront9": 74.2,
      "courseRatingMenBack9": 75.6,
      "slopeMenFront9": 141,
      "slopeMenBack9": 147,
      "courseRatingWomenFront9": "",
      "courseRatingWomenBack9": "",
      "slopeWomenFront9": "",
      "slopeWomenBack9": ""
    },
    {
      "teeID": "106672",
      "teeName": "Gold",
      "teeColor": "#CCCC00",
      "length1": 349,
      "length2": 491,
      "length3": 381,
      "length4": 308,
      "length5": 145,
      "length6": 490,
      "length7": 98,
      "length8": 388,
      "length9": 463,
      "length10": 428,
      "length11": 349,
      "length12": 187,
      "length13": 390,
      "length14": 545,
      "length15": 375,
      "length16": 378,
      "length17": 176,
      "length18": 531,
      "courseRatingMen": 73.4,
      "slopeMen": 137,
      "courseRatingWomen": 78.2,
      "slopeWomen": 146,
      "courseRatingMenFront9": 72.6,
      "courseRatingMenBack9": 74.2,
      "slopeMenFront9": 131,
      "slopeMenBack9": 143,
      "courseRatingWomenFront9": 77,
      "courseRatingWomenBack9": 79.4,
      "slopeWomenFront9": 148,
      "slopeWomenBack9": 144
    },
    {
      "teeID": "106673",
      "teeName": "White",
      "teeColor": "#FFFFFF",
      "length1": 337,
      "length2": 458,
      "length3": 340,
      "length4": 295,
      "length5": 134,
      "length6": 465,
      "length7": 94,
      "length8": 364,
      "length9": 436,
      "length10": 408,
      "length11": 338,
      "length12": 176,
      "length13": 370,
      "length14": 490,
      "length15": 338,
      "length16": 368,
      "length17": 166,
      "length18": 506,
      "courseRatingMen": 71.7,
      "slopeMen": 135,
      "courseRatingWomen": 76.2,
      "slopeWomen": 139,
      "courseRatingMenFront9": 71.2,
      "courseRatingMenBack9": 72.2,
      "slopeMenFront9": 129,
      "slopeMenBack9": 141,
      "courseRatingWomenFront9": 74.6,
      "courseRatingWomenBack9": 77.8,
      "slopeWomenFront9": 139,
      "slopeWomenBack9": 139
    },
    {
      "teeID": "224370",
      "teeName": "Green",
      "teeColor": "#66FF66",
      "length1": 328,
      "length2": 428,
      "length3": 291,
      "length4": 251,
      "length5": 125,
      "length6": 457,
      "length7": 90,
      "length8": 354,
      "length9": 362,
      "length10": 344,
      "length11": 303,
      "length12": 167,
      "length13": 302,
      "length14": 452,
      "length15": 330,
      "length16": 324,
      "length17": 148,
      "length18": 491,
      "courseRatingMen": 68.7,
      "slopeMen": 126,
      "courseRatingWomen": 72.9,
      "slopeWomen": 134,
      "courseRatingMenFront9": 68.2,
      "courseRatingMenBack9": 69.2,
      "slopeMenFront9": 120,
      "slopeMenBack9": 132,
      "courseRatingWomenFront9": 72,
      "courseRatingWomenBack9": 73.8,
      "slopeWomenFront9": 134,
      "slopeWomenBack9": 134
    },
    {
      "teeID": "106674",
      "teeName": "Red",
      "teeColor": "#FF5050",
      "length1": 310,
      "length2": 358,
      "length3": 285,
      "length4": 197,
      "length5": 111,
      "length6": 420,
      "length7": 87,
      "length8": 349,
      "length9": 350,
      "length10": 338,
      "length11": 298,
      "length12": 126,
      "length13": 295,
      "length14": 446,
      "length15": 247,
      "length16": 312,
      "length17": 142,
      "length18": 454,
      "courseRatingMen": 67.3,
      "slopeMen": 124,
      "courseRatingWomen": 71.7,
      "slopeWomen": 132,
      "courseRatingMenFront9": 66.4,
      "courseRatingMenBack9": 68.2,
      "slopeMenFront9": 121,
      "slopeMenBack9": 127,
      "courseRatingWomenFront9": 70.8,
      "courseRatingWomenBack9": 72.6,
      "slopeWomenFront9": 132,
      "slopeWomenBack9": 132
    }
  ],
  "numCoordinates": 100
}""".trimIndent()
    private val coordinatesJson = """{
  "courseID": "012141520658891108829",
  "numCoordinates": 136,
  "coordinates": [
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "1",
      "latitude": 36.570575,
      "longitude": -121.945962
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "1",
      "latitude": 36.5705962,
      "longitude": -121.9461354
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "1",
      "latitude": 36.5705944,
      "longitude": -121.9462765
    },
    {
      "poi": "2",
      "location": "3",
      "sideFW": "1",
      "hole": "1",
      "latitude": 36.5706929,
      "longitude": -121.9459084
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "1",
      "hole": "1",
      "latitude": 36.570695,
      "longitude": -121.9463751
    },
    {
      "poi": "2",
      "location": "3",
      "sideFW": "3",
      "hole": "1",
      "latitude": 36.5704689,
      "longitude": -121.9460076
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "3",
      "hole": "1",
      "latitude": 36.5704839,
      "longitude": -121.9463161
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "1",
      "hole": "1",
      "latitude": 36.5706622,
      "longitude": -121.9470832
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "1",
      "hole": "1",
      "latitude": 36.5706558,
      "longitude": -121.9475338
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "1",
      "latitude": 36.5697438,
      "longitude": -121.9489477
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "1",
      "latitude": 36.5694064,
      "longitude": -121.9495065
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "2",
      "latitude": 36.5694545,
      "longitude": -121.9418075
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "2",
      "latitude": 36.5695688,
      "longitude": -121.9421533
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "2",
      "latitude": 36.5695622,
      "longitude": -121.9424697
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "2",
      "hole": "2",
      "latitude": 36.5695619,
      "longitude": -121.9427347
    },
    {
      "poi": "2",
      "location": "3",
      "sideFW": "3",
      "hole": "2",
      "latitude": 36.5693802,
      "longitude": -121.9418853
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "3",
      "hole": "2",
      "latitude": 36.5693468,
      "longitude": -121.9424713
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "1",
      "hole": "2",
      "latitude": 36.569913,
      "longitude": -121.9437191
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "1",
      "hole": "2",
      "latitude": 36.5700703,
      "longitude": -121.944108
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "3",
      "hole": "2",
      "latitude": 36.5696545,
      "longitude": -121.9439336
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "3",
      "hole": "2",
      "latitude": 36.5698613,
      "longitude": -121.9449341
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "2",
      "latitude": 36.5700613,
      "longitude": -121.9455583
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "2",
      "latitude": 36.5703434,
      "longitude": -121.9471006
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "3",
      "latitude": 36.5673836,
      "longitude": -121.9445853
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "3",
      "latitude": 36.567449,
      "longitude": -121.944535
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "3",
      "latitude": 36.5675355,
      "longitude": -121.9444231
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "3",
      "hole": "3",
      "latitude": 36.5680051,
      "longitude": -121.9442423
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "3",
      "latitude": 36.5691662,
      "longitude": -121.942624
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "3",
      "latitude": 36.5691802,
      "longitude": -121.9412614
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "4",
      "latitude": 36.5661223,
      "longitude": -121.940798
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "4",
      "latitude": 36.566172,
      "longitude": -121.940839
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "4",
      "latitude": 36.566228,
      "longitude": -121.94093
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "3",
      "hole": "4",
      "latitude": 36.5662747,
      "longitude": -121.9413224
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "4",
      "latitude": 36.5671014,
      "longitude": -121.9425542
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "4",
      "latitude": 36.567375,
      "longitude": -121.9439195
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "5",
      "latitude": 36.5649322,
      "longitude": -121.9392905
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "5",
      "latitude": 36.5650613,
      "longitude": -121.9393326
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "5",
      "latitude": 36.5651496,
      "longitude": -121.9393899
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "5",
      "latitude": 36.5657173,
      "longitude": -121.9401121
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "5",
      "latitude": 36.5662451,
      "longitude": -121.9405788
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "6",
      "latitude": 36.562074,
      "longitude": -121.940131
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "6",
      "latitude": 36.56216,
      "longitude": -121.9400431
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "6",
      "latitude": 36.5622477,
      "longitude": -121.9399706
    },
    {
      "poi": "2",
      "location": "3",
      "sideFW": "1",
      "hole": "6",
      "latitude": 36.5620567,
      "longitude": -121.9399574
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "1",
      "hole": "6",
      "latitude": 36.5621744,
      "longitude": -121.9398356
    },
    {
      "poi": "2",
      "location": "3",
      "sideFW": "3",
      "hole": "6",
      "latitude": 36.5622215,
      "longitude": -121.9402095
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "3",
      "hole": "6",
      "latitude": 36.5624108,
      "longitude": -121.9400408
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "1",
      "hole": "6",
      "latitude": 36.5633732,
      "longitude": -121.9389964
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "1",
      "hole": "6",
      "latitude": 36.5639498,
      "longitude": -121.9386799
    },
    {
      "poi": "4",
      "location": "1",
      "sideFW": "3",
      "hole": "6",
      "latitude": 36.5644357,
      "longitude": -121.9391289
    },
    {
      "poi": "5",
      "location": "1",
      "sideFW": "3",
      "hole": "6",
      "latitude": 36.5648544,
      "longitude": -121.9389414
    },
    {
      "poi": "6",
      "location": "2",
      "sideFW": "2",
      "hole": "6",
      "latitude": 36.562811,
      "longitude": -121.9394874
    },
    {
      "poi": "9",
      "location": "2",
      "sideFW": "3",
      "hole": "6",
      "latitude": 36.5633469,
      "longitude": -121.9392427
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "6",
      "latitude": 36.5649503,
      "longitude": -121.9382278
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "6",
      "latitude": 36.5660652,
      "longitude": -121.9378858
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "7",
      "latitude": 36.561275,
      "longitude": -121.9403608
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "7",
      "latitude": 36.5613451,
      "longitude": -121.9403679
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "7",
      "latitude": 36.5614394,
      "longitude": -121.9403847
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "7",
      "latitude": 36.5620646,
      "longitude": -121.9404876
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "7",
      "latitude": 36.5622499,
      "longitude": -121.940509
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "8",
      "latitude": 36.5630373,
      "longitude": -121.9364395
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "8",
      "latitude": 36.5630676,
      "longitude": -121.9365351
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "8",
      "latitude": 36.56304,
      "longitude": -121.936652
    },
    {
      "poi": "9",
      "location": "2",
      "sideFW": "1",
      "hole": "8",
      "latitude": 36.5628436,
      "longitude": -121.938366
    },
    {
      "poi": "9",
      "location": "2",
      "sideFW": "3",
      "hole": "8",
      "latitude": 36.562451,
      "longitude": -121.9380911
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "8",
      "latitude": 36.5618567,
      "longitude": -121.9398358
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "8",
      "latitude": 36.5613084,
      "longitude": -121.9401376
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": "9",
      "latitude": 36.560438,
      "longitude": -121.9329807
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": "9",
      "latitude": 36.560502,
      "longitude": -121.933023
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": "9",
      "latitude": 36.560536,
      "longitude": -121.933125
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "1",
      "hole": "9",
      "latitude": 36.5613938,
      "longitude": -121.9336613
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": "9",
      "latitude": 36.562726,
      "longitude": -121.9350306
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": "9",
      "latitude": 36.5635296,
      "longitude": -121.9368237
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 10,
      "latitude": 36.5578531,
      "longitude": -121.9314008
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 10,
      "latitude": 36.5579593,
      "longitude": -121.9314472
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 10,
      "latitude": 36.55805,
      "longitude": -121.931441
    },
    {
      "poi": "2",
      "location": "1",
      "sideFW": "1",
      "hole": 10,
      "latitude": 36.5581382,
      "longitude": -121.9312906
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "1",
      "hole": 10,
      "latitude": 36.5590248,
      "longitude": -121.9315294
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "1",
      "hole": 10,
      "latitude": 36.559517,
      "longitude": -121.9316689
    },
    {
      "poi": "4",
      "location": "1",
      "sideFW": "3",
      "hole": 10,
      "latitude": 36.558651,
      "longitude": -121.9319156
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 10,
      "latitude": 36.5606772,
      "longitude": -121.932276
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 10,
      "latitude": 36.5616586,
      "longitude": -121.9333905
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 11,
      "latitude": 36.5607915,
      "longitude": -121.9294175
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 11,
      "latitude": 36.5607405,
      "longitude": -121.9295321
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 11,
      "latitude": 36.5606999,
      "longitude": -121.9296578
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 11,
      "latitude": 36.558527,
      "longitude": -121.9308357
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 11,
      "latitude": 36.5578602,
      "longitude": -121.9311173
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 12,
      "latitude": 36.561521,
      "longitude": -121.931138
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 12,
      "latitude": 36.561493,
      "longitude": -121.931038
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 12,
      "latitude": 36.5614905,
      "longitude": -121.9309892
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 12,
      "latitude": 36.5606944,
      "longitude": -121.930326
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 12,
      "latitude": 36.5609863,
      "longitude": -121.9290077
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 13,
      "latitude": 36.5631267,
      "longitude": -121.9348958
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 13,
      "latitude": 36.5630793,
      "longitude": -121.9347986
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 13,
      "latitude": 36.563024,
      "longitude": -121.9347
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "1",
      "hole": 13,
      "latitude": 36.5623757,
      "longitude": -121.9339713
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "3",
      "hole": 13,
      "latitude": 36.5626601,
      "longitude": -121.9337568
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "3",
      "hole": 13,
      "latitude": 36.5625362,
      "longitude": -121.9334027
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 13,
      "latitude": 36.5618459,
      "longitude": -121.9322881
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 13,
      "latitude": 36.5610391,
      "longitude": -121.9310449
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 14,
      "latitude": 36.5672065,
      "longitude": -121.9373511
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 14,
      "latitude": 36.5671089,
      "longitude": -121.9373145
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 14,
      "latitude": 36.56701,
      "longitude": -121.937304
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "3",
      "hole": 14,
      "latitude": 36.5649219,
      "longitude": -121.9372208
    },
    {
      "poi": "9",
      "location": "2",
      "sideFW": "3",
      "hole": 14,
      "latitude": 36.5662931,
      "longitude": -121.9373683
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 14,
      "latitude": 36.5637213,
      "longitude": -121.9366064
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 14,
      "latitude": 36.5630653,
      "longitude": -121.935272
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 15,
      "latitude": 36.569216,
      "longitude": -121.940166
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 15,
      "latitude": 36.5691522,
      "longitude": -121.9400893
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 15,
      "latitude": 36.569091,
      "longitude": -121.939989
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "1",
      "hole": 15,
      "latitude": 36.568716,
      "longitude": -121.9396491
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "1",
      "hole": 15,
      "latitude": 36.5684672,
      "longitude": -121.9392709
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 15,
      "latitude": 36.5676001,
      "longitude": -121.937564
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 15,
      "latitude": 36.56711,
      "longitude": -121.9369685
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 16,
      "latitude": 36.567781,
      "longitude": -121.943366
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 16,
      "latitude": 36.567836,
      "longitude": -121.9432677
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 16,
      "latitude": 36.5678804,
      "longitude": -121.9431484
    },
    {
      "poi": "3",
      "location": "3",
      "sideFW": "2",
      "hole": 16,
      "latitude": 36.5685525,
      "longitude": -121.9415216
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "3",
      "hole": 16,
      "latitude": 36.5683187,
      "longitude": -121.9420097
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 16,
      "latitude": 36.5692351,
      "longitude": -121.9408001
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 16,
      "latitude": 36.5696326,
      "longitude": -121.9399552
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 17,
      "latitude": 36.565646,
      "longitude": -121.944868
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 17,
      "latitude": 36.5657894,
      "longitude": -121.9449199
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 17,
      "latitude": 36.5659977,
      "longitude": -121.9449581
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 17,
      "latitude": 36.5669258,
      "longitude": -121.9442802
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 17,
      "latitude": 36.5671337,
      "longitude": -121.9440871
    },
    {
      "poi": "1",
      "location": "3",
      "sideFW": "2",
      "hole": 18,
      "latitude": 36.5675936,
      "longitude": -121.9497734
    },
    {
      "poi": "1",
      "location": "2",
      "sideFW": "2",
      "hole": 18,
      "latitude": 36.5676012,
      "longitude": -121.9496353
    },
    {
      "poi": "1",
      "location": "1",
      "sideFW": "2",
      "hole": 18,
      "latitude": 36.567592,
      "longitude": -121.949492
    },
    {
      "poi": "3",
      "location": "1",
      "sideFW": "3",
      "hole": 18,
      "latitude": 36.567151,
      "longitude": -121.9466121
    },
    {
      "poi": "4",
      "location": "1",
      "sideFW": "1",
      "hole": 18,
      "latitude": 36.5664066,
      "longitude": -121.9460646
    },
    {
      "poi": "5",
      "location": "1",
      "sideFW": "3",
      "hole": 18,
      "latitude": 36.5671343,
      "longitude": -121.9469412
    },
    {
      "poi": "6",
      "location": "2",
      "sideFW": "2",
      "hole": 18,
      "latitude": 36.5676818,
      "longitude": -121.9485306
    },
    {
      "poi": "9",
      "location": "2",
      "sideFW": "1",
      "hole": 18,
      "latitude": 36.5667349,
      "longitude": -121.9467038
    },
    {
      "poi": "11",
      "location": "2",
      "sideFW": "2",
      "hole": 18,
      "latitude": 36.5661944,
      "longitude": -121.9455972
    },
    {
      "poi": "12",
      "location": "2",
      "sideFW": "2",
      "hole": 18,
      "latitude": 36.5655406,
      "longitude": -121.9451694
    }
  ]
}""".trimIndent()

    val clubs: Clubs = clubsJson.fromJsonString<Clubs>() ?: Clubs()
    val club: Club = clubJson.fromJsonString<Club>() ?: Club()
    val course: Course = courseJson.fromJsonString<Course>() ?: Course()
    val coordinates: Coordinates = coordinatesJson.fromJsonString<Coordinates>() ?: Coordinates()

}
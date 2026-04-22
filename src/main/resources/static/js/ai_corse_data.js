const aiCourses = [
    {
        id: 1,
        title: "대전 유명 빵집 코스",
        type: "bus",
        bakeries: [5, 1, 4, 2, 3],
        routeSteps: [
            {
                fromId: 5,
                toId: 1,
                walkTime: 10
            },
            {
                fromId: 1,
                toId: 4,
                walkTime: 4
            },
            {
                fromId: 4,
                toId: 2,
                busTime: 30
            },
            {
                fromId: 2,
                toId: 3,
                busTime: 20
            }
        ]
    },

    {
        id: 2,
        title: "대전 유성 빵집 코스",
        type: "car",
        bakeries: [3, 5, 4],
        routeSteps: [
            {
                fromId: 3,
                toId: 5,
                carTime: 12
            },
            {
                fromId: 5,
                toId: 4,
                carTime: 18
            }
        ]
    },

    {
        id: 3,
        title: "베이커리 전문 카페 코스",
        type: "bus",
        bakeries: [5, 3, 4],
        routeSteps: [
            {
                fromId: 5,
                toId: 3,
                walkTime: 8
            },
            {
                fromId: 3,
                toId: 4,
                busTime: 20
            }
        ]
    },

    {
        id: 4,
        title: "가족 나들이 추천 코스",
        type: "car",
        bakeries: [1, 2, 5],
        routeSteps: [
            {
                fromId: 1,
                toId: 2,
                carTime: 12
            },
            {
                fromId: 2,
                toId: 5,
                carTime: 15
            }
        ]
    }
];
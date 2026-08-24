window.addEventListener('load', function () {
    // 페이지의 모든 리소스(이미지 등)까지 다 로드된 후 실핼
    // DOMContentLoaded보다 늦게 실행되는데, box 크기 계산할 때 정확한 크기가 필요해서 load를 씀

    const box = document.querySelector('.white-box');
    // 알약(pill)들이 떨어질 공간이 되는 흰 박스 요소를 찾음

    const pills = Array.from(document.querySelectorAll('.pill'));
    // .pill 클래스를 가진 요소들을 전부 찾아서 배열로 변환함
    // querySelectorAll은 NodeList를 반환하는데 forEach, map 같은 배열 메서드 쓰려고 배열로 바꾼 거임

    if (!box || pills.length === 0) return;
    // box가 없거나 pill이 하나도 없으면 아예 실행 안 하고 종료(에러 방지용)

    function startFallingPills() {
        // 실제로 물리 시뮬레이션을 시작하는 함수

        const { Engine, Runner, Bodies, Composite, Events } = Matter;
        // Matter.js 라이브러리에서 필요한 모듈들만 꺼내옴
        // Engine: 물리 엔진 자체, Runner: 엔진을 계속 돌려주는 역할
        // Bodies: 사각형/원 같은 물리 객체 만드는 도구, Composite: 월드에 객체 추가/삭제
        // Events: 매 프레임마다 특정 시점에 콜백 실행할 수 있게 해줌 (여기선 각도 제한용)

        const boxRect = box.getBoundingClientRect();
        // box의 실제 화면상 크기와 위치 정보를 가져옴 (너비, 높이, 위치 등)

        const boxWidth = boxRect.width;
        const boxHeight = boxRect.height;
        // 박스의 너비/높이 값만 따로 변수에 저장함 (이후 계산에 계속 쓰임)

        const engine = Engine.create({
            positionIterations: 10,
            velocityIterations: 8
        });
        // 물리 엔진 생성함
        // positionIterations/velocityIterations는 정밀도 관련 옵션임
        // 숫자가 높을수록 물체끼리 겹치거나 뚫고 지나가는 걸 더 정확하게 계산하지만 연산량은 늘어남

        engine.gravity.y = 1;
        // 중력 세기를 설정함. y값이 양수면 아래 방향으로 중력이 작용함 (기본값이 1)

        engine.timing.timeScale = 0.7;
        // 시뮬레이션 속도를 조절함. 1이 기본 속도고 0.7이면 좀 더 느리게 움직임 (슬로우모션 느낌)

        const world = engine.world;
        // 엔진 안에 있는 world 객체를 꺼냄. 물체들은 다 이 world 안에 추가됨

        const wallOptions = { isStatic: true, restitution: 0.5, friction: 0.4 };
        // 벽(바닥/좌우벽)에 공통으로 쓸 옵션임
        // isStatic: true면 이 객체는 중력 영향 안 받고 고정되어 있음 (안 움직임)
        // restitution: 반발력, 튕기는 정도 (0.5면 어느정도 통통 튕김)
        // friction: 마찰력, 표면 미끄러짐 정도

        const wallThickness = 40;
        // 벽 두께를 40px로 설정함

        const ground = Bodies.rectangle(boxWidth / 2, boxHeight + 10, boxWidth, 20, wallOptions);
        // 바닥 역할을 할 사각형을 만듦
        // 위치는 박스 가로 중앙, 세로로는 박스 바닥보다 살짝(10px) 아래
        // 크기는 박스 너비만큼 가로로 길고 높이는 20px

        const leftWall = Bodies.rectangle(-wallThickness / 2, boxHeight / 2, wallThickness, boxHeight * 2, wallOptions);
        // 왼쪽 벽 생성. 박스 왼쪽 바깥으로 살짝 걸치게 배치해서 pill이 왼쪽으로 빠져나가지 못하게 막음
        // 높이를 boxHeight*2로 넉넉하게 잡아서 위에서 떨어지는 pill도 놓치지 않게 함

        const rightWall = Bodies.rectangle(boxWidth + wallThickness / 2, boxHeight / 2, wallThickness, boxHeight * 2, wallOptions);
        // 오른쪽 벽도 같은 방식으로 생성함 (leftWall과 대칭)

        Composite.add(world, [ground, leftWall, rightWall]);
        // 만든 바닥/좌우벽을 world에 추가함. 이제 이 물체들이 시뮬레이션에 포함됨

        const pillBodies = pills.map((pill, i) => {
            // 각 pill 요소마다 대응하는 물리 객체(body)를 만들어서 배열로 반환함
            // map을 쓰는 이유: DOM 요소 각각에 대해 물리적 몸체를 하나씩 매칭시켜야 하니까

            const rect = pill.getBoundingClientRect();
            // 실제 pill 요소의 크기(너비/높이)를 화면 기준으로 가져옴

            const w = rect.width;
            const h = rect.height;
            // pill의 너비/높이를 변수에 저장

            const minX = w / 2 + 10;
            const maxX = boxWidth - w / 2 - 10;
            // pill이 떨어질 때 시작할 x좌표의 최소/최대 범위를 계산함
            // pill 너비의 절반만큼 여유를 줘야 벽에 안 걸치고, 추가로 10px씩 더 여유를 둠

            const startX = minX < maxX ? (minX + Math.random() * (maxX - minX)) : (boxWidth / 2);
            // minX가 maxX보다 작으면(정상적인 경우) 그 범위 안에서 랜덤한 x좌표를 정함
            // 혹시 pill이 박스보다 커서 범위가 이상해지면 그냥 박스 중앙에서 시작하게 예외처리함

            const startY = -60 - (i * 90);
            // 시작 y좌표는 박스 위쪽 바깥(화면 밖)으로 설정함
            // i(순서)마다 90px씩 더 위에서 시작하게 해서 pill들이 한꺼번에 안 겹치고 순차적으로 떨어지게 함

            const body = Bodies.rectangle(startX, startY, w, h, {
                chamfer: { radius: h / 2 },
                restitution: 0.3,
                friction: 0.4,
                frictionAir: 0.01,
                density: 0.002
            });
            // 실제 물리 몸체(사각형)를 생성함
            // chamfer: 모서리를 둥글게 깎는 옵션. radius를 높이의 절반으로 주면 알약처럼 양끝이 완전히 둥글게 됨
            // restitution: 튕기는 정도 (벽보다 낮게 줘서 덜 튕기게 함)
            // friction: 표면 마찰
            // frictionAir: 공기 저항 비슷한 개념, 낙하하면서 서서히 감속되는 효과
            // density: 밀도, 높을수록 무거워짐 (다른 pill 밀어내는 힘에 영향)

            Matter.Body.setAngularVelocity(body, (Math.random() - 0.5) * 0.1);
            // 생성한 몸체에 아주 약간의 회전 속도를 랜덤하게 부여함
            // (Math.random() - 0.5)는 -0.5~0.5 사이 값이 나오니까 결국 -0.05~0.05 사이의 미세한 회전값이 됨
            // 이렇게 하면 pill들이 완전히 똑같이 안 떨어지고 자연스럽게 살짝씩 돌면서 떨어짐

            pill.style.width = w + 'px';
            pill.style.height = h + 'px';
            // DOM 요소의 실제 css 크기를 물리 몸체 크기와 동일하게 고정시킴
            // (레이아웃 변화로 크기가 틀어지는 걸 방지하려는 목적)

            return { el: pill, body, w, h };
            // DOM 요소(el)와 물리 몸체(body), 크기 정보를 묶어서 객체로 반환함
            // 나중에 물리 계산 결과를 다시 DOM에 반영할 때 이 정보들이 필요함
        });

        Composite.add(world, pillBodies.map(p => p.body));
        // pillBodies 배열에서 body만 뽑아내서 한꺼번에 world에 추가함
        // 이제 pill들도 중력 영향을 받으면서 떨어지기 시작함

        Events.on(engine, 'beforeUpdate', function () {
            // 엔진이 매 프레임 물리 계산을 하기 '직전'에 실행되는 콜백을 등록함
            // 여기서 각도를 강제로 제한해서 pill이 뒤집히지 않게 만듦

            const maxAngle = 0.6;
            // 허용할 최대 회전 각도 (라디안 단위, 0.6 rad ≈ 34도)

            pillBodies.forEach(({ body }) => {
                // 모든 pill 몸체를 순회하면서 각도를 체크함

                if (body.angle > maxAngle) {
                    Matter.Body.setAngle(body, maxAngle);
                    Matter.Body.setAngularVelocity(body, 0);
                    // 각도가 최대치를 넘으면 강제로 maxAngle로 고정하고 회전 속도도 0으로 만들어서 더 안 돌아가게 막음
                } else if (body.angle < -maxAngle) {
                    Matter.Body.setAngle(body, -maxAngle);
                    Matter.Body.setAngularVelocity(body, 0);
                    // 반대 방향으로도 똑같이 최소 각도로 제한함
                }
            });
        });

        const runner = Runner.create();
        // 엔진을 지속적으로 업데이트해줄 러너를 생성함

        Runner.run(runner, engine);
        // 러너를 실행시켜서 엔진이 계속 물리 계산을 하도록 만듦 (내부적으로 requestAnimationFrame 기반으로 돌아감)

        (function updateLoop() {
            // 즉시실행함수로 매 프레임마다 화면(DOM)을 갱신하는 루프를 만듦
            // Matter.js는 물리 좌표만 계산할 뿐 화면에 그려주진 않아서 직접 DOM에 반영해줘야 함

            pillBodies.forEach(({ el, body, w, h }) => {
                // 모든 pill에 대해 물리 계산 결과를 실제 화면 위치로 옮겨줌

                const x = body.position.x - w / 2;
                const y = body.position.y - h / 2;
                // Matter.js의 body.position은 몸체의 '중심' 좌표임
                // 근데 CSS translate는 왼쪽 위 기준이라서 너비/높이 절반만큼 빼서 보정해줌

                const angle = body.angle * (180 / Math.PI);
                // body.angle은 라디안 단위라서 CSS에서 쓰는 degree 단위로 변환함

                el.style.transform = `translate(${x}px, ${y}px) rotate(${angle}deg)`;
                // 계산한 위치와 각도를 실제 DOM 요소에 transform으로 적용함
                // 이 한 줄 덕분에 물리엔진 계산 결과가 눈에 보이는 애니메이션이 됨
            });

            requestAnimationFrame(updateLoop);
            // 다음 프레임에도 이 함수가 다시 실행되도록 예약함 (무한 루프처럼 계속 반복됨)
        })();
    }

    const observer = new IntersectionObserver((entries, observerInstance) => {
        // box가 화면에 보이는지 감지하는 IntersectionObserver를 만듦
        // 화면에 안 보이는데 미리 물리 연산 돌리면 자원 낭비니까, 보일 때만 시작하려는 목적

        entries.forEach(entry => {
            // 관찰 대상(여기선 box) 하나에 대한 정보가 entry로 들어옴

            if (entry.isIntersecting) {
                // box가 화면(viewport)에 실제로 보이기 시작했다면

                pills.forEach(pill => {
                    pill.style.opacity = '1';
                    // 처음엔 숨겨져 있던(opacity 0이었을) pill들을 보이게 만듦
                });

                startFallingPills();
                // 물리 시뮬레이션 시작 함수를 호출함

                observerInstance.unobserve(entry.target);
                // 한 번 실행됐으면 더 이상 관찰할 필요 없으니까 감시를 중단함 (성능 최적화)
            }
        });
    }, {
        threshold: 0.7
        // box가 70% 이상 화면에 보였을 때 isIntersecting이 true가 되도록 설정함
    });

    observer.observe(box);
    // box 요소를 실제로 관찰 대상으로 등록해서 감시를 시작함
});
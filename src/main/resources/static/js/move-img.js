// img-box 안의 img 태그들을 순서대로 가져옴 (순서 = uni, aca, it, lib)
// class를 사용해도 되지만 여러개의 이미지 중에서 특정 구역 안에있는 이미지들만 가져오기 위해
    const cards = document.querySelectorAll('#imgBox img');
    //  const cards = document.querySelectorAll('.img-box img'); 이렇게 적어도됨!

    // 4개 슬롯의 위치값: 앞쪽 1장 + 뒤쪽 3장
    const positions = [
    { left: 120, z: 4, scale: 1 },     // 맨 앞
    { left: 20,  z: 3, scale: 0.9 },   // 왼쪽 뒤
    { left: 220, z: 3, scale: 0.9 },   // 오른쪽 뒤
    { left: 70,  z: 2, scale: 0.82 }   // 가장 뒤
    ];


    function apply(order) {
    cards.forEach((card, i) => {
        const p = positions[order[i]];
        card.style.left = p.left + 'px';
        card.style.zIndex = p.z;
        card.style.transform = `scale(${p.scale})`;
    });
}

    // 카드 4장의 순서를 담는 배열 (인덱스가 곧 img-box 안의 img 순서)
    let order = [0, 1, 2, 3];
    apply(order);

    // 2.2초마다 순서를 한 칸씩 밀어서 자리 교체
    setInterval(() => {
        // setImterval : 일정한 시간 간격 마다 정해진 행동(아래 부분 참고)을 무한히 반복하는 함수
        // setInterval(실행할_함수, 시간_간격);
    order.push(order.shift());  //순서 바꾸기
    apply(order);  //화면에 다시 나타내라
}, 2200);
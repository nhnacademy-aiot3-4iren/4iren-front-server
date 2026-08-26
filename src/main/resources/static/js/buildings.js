const openBuildingModalBtn = document.getElementById('openBuildingModalBtn');
const closeBuildingModalBtn = document.getElementById('closeBuildingModalBtn');
const createBuildingBtn = document.getElementById('createBuildingBtn');
const searchAddressBtn = document.getElementById('searchAddressBtn');
const buildingModalOverlay = document.getElementById('buildingModalOverlay');

const buildingNameInput = document.getElementById('buildingNameInput');
const buildingDescInput = document.getElementById('buildingDescInput');
const roadAddressInput = document.getElementById('roadAddressInput');
const detailAddressInput = document.getElementById('detailAddressInput');
const regionNameInput = document.getElementById('regionNameInput');

if (openBuildingModalBtn) {
openBuildingModalBtn.addEventListener('click', () => {
    buildingModalOverlay.classList.add('active');
    buildingNameInput.focus();
});
}

closeBuildingModalBtn.addEventListener('click', closeBuildingModal);

searchAddressBtn.addEventListener('click', openPostcodeSearch);

buildingModalOverlay.addEventListener('click', event => {
    if (event.target === buildingModalOverlay) {
        closeBuildingModal();
    }
});

createBuildingBtn.addEventListener('click', async () => {
    const buildingName = buildingNameInput.value.trim();

    if (!buildingName) {
        alert('건물 이름을 입력해주세요.');
        buildingNameInput.focus();
        return;
    }

    try {
        createBuildingBtn.disabled = true;
        const response = await fetch(`/api/front/teams/${teamId}/buildings`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                buildingName,
                description: buildingDescInput.value.trim(),
                roadAddress: roadAddressInput.value.trim(),
                detailAddress: detailAddressInput.value.trim(),
                regionName: regionNameInput.value.trim()
            })
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        window.location.reload();
    } catch (error) {
        alert(error.message || '건물 생성에 실패했습니다.');
    } finally {
        createBuildingBtn.disabled = false;
    }
});

document.querySelectorAll('.building-delete-btn').forEach(button => {
    button.addEventListener('click', async () => {
        const buildingId = button.dataset.buildingId;
        const buildingName = button.dataset.buildingName || '건물';

        if (!confirm(`${buildingName}을(를) 삭제할까요?`)) {
            return;
        }

        try {
            button.disabled = true;
            const response = await fetch(`/api/front/teams/${teamId}/buildings/${buildingId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            window.location.reload();
        } catch (error) {
            alert(error.message || '건물 삭제에 실패했습니다.');
        } finally {
            button.disabled = false;
        }
    });
});

function closeBuildingModal() {
    buildingModalOverlay.classList.remove('active');
    buildingNameInput.value = '';
    buildingDescInput.value = '';
    roadAddressInput.value = '';
    detailAddressInput.value = '';
    regionNameInput.value = '';
}

function openPostcodeSearch() {
    if (!globalThis.kakao || !globalThis.kakao.Postcode) {
        alert('주소 검색 서비스를 불러오지 못했습니다.');
        return;
    }

    new kakao.Postcode({
        oncomplete: data => {
            roadAddressInput.value = data.roadAddress || data.address || '';
            regionNameInput.value = [data.sido, data.sigungu].filter(Boolean).join(' ');
            detailAddressInput.focus();
        }
    }).open();
}
